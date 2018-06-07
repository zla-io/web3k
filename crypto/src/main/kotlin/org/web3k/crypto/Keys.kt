package org.web3k.crypto

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.math.ec.ECPoint
import org.web3k.common.clean0xPrefix
import org.web3k.common.hexToByteArray
import org.web3k.common.keccak
import org.web3k.common.prepend0xPrefix
import org.web3k.common.toBytesPadded
import org.web3k.common.toHexString
import org.web3k.common.toHexStringZeroPadded
import org.web3k.random.secureRandom
import java.math.BigInteger
import java.security.InvalidAlgorithmParameterException
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.Security
import java.security.spec.ECGenParameterSpec
import java.util.*

const val PRIVATE_KEY_SIZE = 32
const val PUBLIC_KEY_SIZE = 64
const val ADDRESS_LENGTH_IN_HEX = 40
const val PUBLIC_KEY_LENGTH_IN_HEX = PUBLIC_KEY_SIZE shl 1

/** Wallet address prefixed by 0x. */
typealias Address = String

fun ECKeyPair.publicKeyToAddress(): Address =
        publicKey.publicKeyToAddress()

@Throws(InvalidAlgorithmParameterException::class, NoSuchAlgorithmException::class, NoSuchProviderException::class)
fun createEcKeyPair(): ECKeyPair =
        createSecp256k1KeyPair().toECKeyPair()

fun BigInteger.publicKeyToAddress(): Address =
        toHexStringZeroPadded(PUBLIC_KEY_LENGTH_IN_HEX, true)
                .publicKeyToAddress()

fun String.publicKeyToAddress(): Address {
    var publicKeyNoPrefix = this.clean0xPrefix()

    if (publicKeyNoPrefix.length < PUBLIC_KEY_LENGTH_IN_HEX) {
        publicKeyNoPrefix = "0".repeat(PUBLIC_KEY_LENGTH_IN_HEX - publicKeyNoPrefix.length) + publicKeyNoPrefix
    }
    val hexToByteArray = publicKeyNoPrefix.hexToByteArray()
    val hash = hexToByteArray.keccak()
            .toHexString()
            .clean0xPrefix()

    return hash.substring(hash.length - ADDRESS_LENGTH_IN_HEX) // right most 160 bits
            .prepend0xPrefix()
}

fun ByteArray.publicKeyToAddress(): ByteArray {
    val hash = this.keccak()
    return Arrays.copyOfRange(hash, hash.size - 20, hash.size)  // right most 160 bits
}

fun ECKeyPair.getCompressedPublicKey(): ByteArray {
    //add the uncompressed prefix
    val ret = publicKey.toBytesPadded(PUBLIC_KEY_SIZE + 1)
    ret[0] = 4
    val point = CURVE.curve.decodePoint(ret)
    return point.getEncoded(true)
}

/**
 * Takes a public key in compressed encoding (including prefix)
 * and returns the key in uncompressed encoding (without prefix)
 */
fun ByteArray.decompressKey(): ByteArray {
    val point = CURVE.curve.decodePoint(this)
    val encoded = point.getEncoded(false)
    return Arrays.copyOfRange(encoded, 1, encoded.size)
}

/** Decodes an uncompressed public key (without 0x04 prefix) given an ECPoint */
fun ECPoint.toPublicKey(): BigInteger {
    val encoded = getEncoded(false)
    return BigInteger(1, Arrays.copyOfRange(encoded, 1, encoded.size))
}

/**
 * Create a keypair using SECP-256k1 curve.
 *
 * Private keypairs are encoded using PKCS8 and
 * private keys are encoded using X.509,
 * that's why [setupAdvancedCrypto] is a requirement.
 */
@Throws(NoSuchProviderException::class, NoSuchAlgorithmException::class, InvalidAlgorithmParameterException::class)
internal fun createSecp256k1KeyPair(): KeyPair {
    setupAdvancedCrypto()
    val keyPairGenerator = KeyPairGenerator.getInstance("ECDSA")
    val ecGenParameterSpec = ECGenParameterSpec("secp256k1")
    keyPairGenerator.initialize(ecGenParameterSpec, secureRandom)
    return keyPairGenerator.generateKeyPair()
}

/** Install required security provider for advanced crypto , necessary for [createSecp256k1KeyPair]. */
internal fun setupAdvancedCrypto() {
    Security.insertProviderAt(BouncyCastleProvider(), 1)
}
