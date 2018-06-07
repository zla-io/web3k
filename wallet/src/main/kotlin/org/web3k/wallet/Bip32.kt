package org.web3k.wallet

import org.web3k.common.ripemd160
import org.web3k.common.sha256
import org.web3k.common.toBytesPadded
import org.web3k.crypto.*
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.KeyException
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

//
// Implementation of [BIP-32](https://github.com/bitcoin/bips/blob/master/bip-0032.mediawiki).
//

fun generateKey(seed: ByteArray, path: String): ExtendedKey {
    val master = seed.extendedKeyFromSeed()

    var child = master
    path.toBip44().toInts().forEach {
        child = child.generateChildKey(it)
    }

    return child
}

data class ExtendedKey(
        val keyPair: ECKeyPair,
        private val chainCode: ByteArray,
        private val depth: Byte,
        private val parentFingerprint: Int,
        private val sequence: Int
) {
    fun generateChildKey(element: Int): ExtendedKey {
        require(element.isHardened.not() || keyPair.privateKey != BigInteger.ZERO) {
            "hardened path can't be resolved without a private key"
        }

        val mac = Mac.getInstance("HmacSHA512")
        val key = SecretKeySpec(chainCode, "HmacSHA512")
        mac.init(key)

        val extended: ByteArray
        val pub = keyPair.getCompressedPublicKey()
        if (element.isHardened) {
            val privateKeyPaddedBytes = keyPair.privateKey.toBytesPadded(PRIVATE_KEY_SIZE)

            extended = ByteBuffer
                    .allocate(privateKeyPaddedBytes.size + 5)
                    .order(ByteOrder.BIG_ENDIAN)
                    .put(0)
                    .put(privateKeyPaddedBytes)
                    .putInt(element)
                    .array()
        } else {
            // non-hardened
            extended = ByteBuffer
                    .allocate(pub.size + 4)
                    .order(ByteOrder.BIG_ENDIAN)
                    .put(pub)
                    .putInt(element)
                    .array()
        }
        val lr = mac.doFinal(extended)
        val l = Arrays.copyOfRange(lr, 0, PRIVATE_KEY_SIZE)
        val r = Arrays.copyOfRange(lr, PRIVATE_KEY_SIZE, PRIVATE_KEY_SIZE + CHAINCODE_SIZE)

        val m = BigInteger(1, l)
        if (m >= CURVE.n) {
            throw KeyException("Child key derivation resulted in a key with higher modulus. Suggest deriving the next increment.")
        }

        return if (keyPair.privateKey != BigInteger.ZERO) {
            val k = m.add(keyPair.privateKey).mod(CURVE.n)
            if (k == BigInteger.ZERO) {
                throw KeyException("Child key derivation resulted in zeros. Suggest deriving the next increment.")
            }
            ExtendedKey(k.toECKeyPair(), r, (depth + 1).toByte(), keyPair.computeFingerPrint(), element)
        } else {
            val q = CURVE.g.multiply(m).add(CURVE.curve.decodePoint(pub)).normalize()
            if (q.isInfinity) {
                throw KeyException("Child key derivation resulted in zeros. Suggest deriving the next increment.")
            }
            val point = CURVE.curve.createPoint(q.xCoord.toBigInteger(), q.yCoord.toBigInteger())

            ExtendedKey(ECKeyPair(BigInteger.ZERO, point.toPublicKey()), r, (depth + 1).toByte(), keyPair.computeFingerPrint(), element)
        }
    }

    fun serialize(publicKeyOnly: Boolean = false): String =
            ByteBuffer.allocate(EXTENDED_KEY_SIZE)
                    .apply {
                        if (publicKeyOnly || keyPair.privateKey == BigInteger.ZERO) {
                            put(xpub)
                        } else {
                            put(xprv)
                        }
                        put(depth)
                        putInt(parentFingerprint)
                        putInt(sequence)
                        put(chainCode)
                        if (publicKeyOnly || keyPair.privateKey == BigInteger.ZERO) {
                            put(keyPair.getCompressedPublicKey())
                        } else {
                            put(0x00)
                            put(keyPair.privateKey.toBytesPadded(PRIVATE_KEY_SIZE))
                        }
                    }
                    .array()
                    .encodeToBase58WithChecksum()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExtendedKey

        if (keyPair != other.keyPair) return false
        if (!Arrays.equals(chainCode, other.chainCode)) return false
        if (depth != other.depth) return false
        if (parentFingerprint != other.parentFingerprint) return false
        if (sequence != other.sequence) return false

        return true
    }

    override fun hashCode(): Int {
        var result = keyPair.hashCode()
        result = 31 * result + Arrays.hashCode(chainCode)
        result = 31 * result + depth
        result = 31 * result + parentFingerprint
        result = 31 * result + sequence
        return result
    }
}

fun ByteArray.extendedKeyFromSeed(publicKeyOnly: Boolean = false): ExtendedKey {
    val mac = Mac.getInstance("HmacSHA512")
    val seedKey = SecretKeySpec(BITCOIN_SEED, "HmacSHA512")
    mac.init(seedKey)
    val lr = mac.doFinal(this)
    val l = Arrays.copyOfRange(lr, 0, PRIVATE_KEY_SIZE)
    val r = Arrays.copyOfRange(lr, PRIVATE_KEY_SIZE, PRIVATE_KEY_SIZE + CHAINCODE_SIZE)
    val m = BigInteger(1, l)
    if (m >= CURVE.n) {
        throw KeyException("Master key creation resulted in a key with higher modulus. Suggest deriving the next increment.")
    }
    val keyPair = l.toECKeyPair()
    return if (publicKeyOnly) {
        val pubKeyPair = ECKeyPair(BigInteger.ZERO, keyPair.publicKey)
        ExtendedKey(pubKeyPair, r, 0, 0, 0)
    } else {
        ExtendedKey(keyPair, r, 0, 0, 0)
    }
}

/**
 * Gets an [Int] representation of public key hash
 * @return an Int built from the first 4 bytes of the result of hash160 over the compressed public key
 */
private fun ECKeyPair.computeFingerPrint(): Int {
    val pubKeyHash = getCompressedPublicKey()
            .sha256()
            .ripemd160()
    var fingerprint = 0
    for (i in 0..3) {
        fingerprint = fingerprint shl 8
        fingerprint = fingerprint or (pubKeyHash[i].toInt() and 0xff)
    }
    return fingerprint
}

fun String.toExtendedKey(): ExtendedKey {
    val data = this.decodeBase58WithChecksum()
    if (data.size != EXTENDED_KEY_SIZE) {
        throw KeyException("invalid extended key")
    }

    val buff = ByteBuffer
            .wrap(data)
            .order(ByteOrder.BIG_ENDIAN)

    val type = ByteArray(4)

    buff.get(type)

    val hasPrivate = when {
        Arrays.equals(type, xprv) -> true
        Arrays.equals(type, xpub) -> false
        else -> throw KeyException("invalid magic number for an extended key")
    }

    val depth = buff.get()
    val parent = buff.int
    val sequence = buff.int

    val chainCode = ByteArray(PRIVATE_KEY_SIZE)
    buff.get(chainCode)

    val keyPair = if (hasPrivate) {
        buff.get() // ignore the leading 0
        val privateBytes = ByteArray(PRIVATE_KEY_SIZE)
        buff.get(privateBytes)
        privateBytes.toECKeyPair()
    } else {
        val compressedPublicBytes = ByteArray(COMPRESSED_PUBLIC_KEY_SIZE)
        buff.get(compressedPublicBytes)
        val uncompressedPublicBytes = compressedPublicBytes.decompressKey()
        ECKeyPair(BigInteger.ZERO, BigInteger(1, uncompressedPublicBytes))
    }
    return ExtendedKey(keyPair, chainCode, depth, parent, sequence)
}

private val BITCOIN_SEED = "Bitcoin seed".toByteArray()
private const val CHAINCODE_SIZE = PRIVATE_KEY_SIZE
private const val COMPRESSED_PUBLIC_KEY_SIZE = PRIVATE_KEY_SIZE + 1
private const val EXTENDED_KEY_SIZE: Int = 78
internal val xprv = byteArrayOf(0x04, 0x88.toByte(), 0xAD.toByte(), 0xE4.toByte())
internal val xpub = byteArrayOf(0x04, 0x88.toByte(), 0xB2.toByte(), 0x1E.toByte())
