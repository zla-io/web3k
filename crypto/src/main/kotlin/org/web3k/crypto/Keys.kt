package org.web3k.crypto

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.math.ec.ECPoint
import org.web3k.common.*
import org.web3k.random.secureRandom
import java.math.BigInteger
import java.security.*
import java.security.spec.ECGenParameterSpec
import java.util.*

const val PRIVATE_KEY_SIZE = 32
const val PUBLIC_KEY_SIZE = 64
const val ADDRESS_LENGTH_IN_HEX = 40
const val PUBLIC_KEY_LENGTH_IN_HEX = PUBLIC_KEY_SIZE shl 1

typealias Address = String

fun ECKeyPair.getAddress(): Address =
        getAddress(publicKey)

// TODO: what's different between this one and SecureRandomProvider?
fun initializeCrypto() {
    Security.insertProviderAt(BouncyCastleProvider(), 1)
}

@Throws(InvalidAlgorithmParameterException::class, NoSuchAlgorithmException::class, NoSuchProviderException::class)
fun createEcKeyPair(): ECKeyPair =
        createSecp256k1KeyPair().toECKeyPair()

fun getAddress(publicKey: BigInteger): Address =
        getAddress(publicKey.toHexStringZeroPadded(PUBLIC_KEY_LENGTH_IN_HEX))

fun getAddress(publicKey: String): String {
    var publicKeyNoPrefix = publicKey.clean0xPrefix()

    if (publicKeyNoPrefix.length < PUBLIC_KEY_LENGTH_IN_HEX) {
        publicKeyNoPrefix = "0".repeat(PUBLIC_KEY_LENGTH_IN_HEX - publicKeyNoPrefix.length) + publicKeyNoPrefix
    }
    val hexToByteArray = publicKeyNoPrefix.hexToByteArray()
    val hash = hexToByteArray.keccak().toHexString()

    return hash.substring(hash.length - ADDRESS_LENGTH_IN_HEX)  // right most 160 bits
}

fun getAddress(publicKey: ByteArray): ByteArray {
    val hash = publicKey.keccak()
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
fun decompressKey(publicBytes: ByteArray): ByteArray {
    val point = CURVE.curve.decodePoint(publicBytes)
    val encoded = point.getEncoded(false)
    return Arrays.copyOfRange(encoded, 1, encoded.size)
}

/**
 * Decodes an uncompressed public key (without 0x04 prefix) given an ECPoint
 */
fun ECPoint.toPublicKey(): BigInteger {
    val encoded = getEncoded(false)
    return BigInteger(1, Arrays.copyOfRange(encoded, 1, encoded.size))
}

/**
 * Create a keypair using SECP-256k1 curve.
 *
 * Private keypairs are encoded using PKCS8
 *
 * Private keys are encoded using X.509
 */
@Throws(NoSuchProviderException::class, NoSuchAlgorithmException::class, InvalidAlgorithmParameterException::class)
internal fun createSecp256k1KeyPair(): KeyPair {
    val keyPairGenerator = KeyPairGenerator.getInstance("ECDSA")
    val ecGenParameterSpec = ECGenParameterSpec("secp256k1")
    keyPairGenerator.initialize(ecGenParameterSpec, secureRandom)
    return keyPairGenerator.generateKeyPair()
}
