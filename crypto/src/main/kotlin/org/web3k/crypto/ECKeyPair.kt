package org.web3k.crypto

import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.params.ECPrivateKeyParameters
import org.bouncycastle.crypto.signers.ECDSASigner
import org.bouncycastle.crypto.signers.HMacDSAKCalculator
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
import org.web3k.common.toBigInt
import java.math.BigInteger
import java.security.KeyPair
import java.util.*

/**
 * Elliptic Curve SECP-256k1 generated key pair.
 */
data class ECKeyPair(
        val privateKey: BigInteger,
        val publicKey: BigInteger
)

/**
 * Sign a hash with the private key of this key pair.
 * @param transactionHash   the hash to sign
 * @return  An [ECDSASignature] of the hash
 */
fun ECKeyPair.sign(transactionHash: ByteArray): ECDSASignature {
    val signer = ECDSASigner(HMacDSAKCalculator(SHA256Digest()))

    signer.init(true, ECPrivateKeyParameters(privateKey, CURVE))
    val components = signer.generateSignature(transactionHash)

    return ECDSASignature(components[0], components[1]).toCanonical()
}

fun KeyPair.toECKeyPair(): ECKeyPair =
        ECKeyPair(
                privateKey = (private as BCECPrivateKey).d,
                // Ethereum does not use encoded public keys like bitcoin - see
                // https://en.bitcoin.it/wiki/Elliptic_Curve_Digital_Signature_Algorithm for details
                // Additionally, as the first bit is a constant prefix (0x04) we ignore this value
                publicKey = (public as BCECPublicKey).q
                        .getEncoded(false)
                        .let { publicKeyBytes ->
                            BigInteger(1, Arrays.copyOfRange(publicKeyBytes, 1, publicKeyBytes.size))
                        }
        )

fun BigInteger.toECKeyPair(): ECKeyPair =
        ECKeyPair(this, publicKeyFromPrivate(this))

fun ByteArray.toECKeyPair(): ECKeyPair =
        toBigInt(this).toECKeyPair()
