package org.web3k.common

import org.bouncycastle.jcajce.provider.digest.Keccak
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Keccak-256 hash function.
 *
 * @param hexInput hex encoded input data with optional 0x prefix
 * @return hash value as hex encoded string
 */
fun sha3(hexInput: String): String {
    val bytes = hexStringToByteArray(hexInput)
    val result = sha3(bytes)
    return toHexString(result)
}

/**
 * Keccak-256 hash function.
 *
 * @param input binary encoded input data
 * @param offset of start of data
 * @param length of data
 * @return hash value
 */
fun sha3(input: ByteArray, offset: Int = 0, length: Int = input.size): ByteArray =
        Keccak.Digest256().run {
            update(input, offset, length)
            digest()
        }

/**
 * Keccak-256 hash function that operates on a UTF-8 encoded String.
 *
 * @param utf8String UTF-8 encoded string
 * @return hash value as hex encoded string
 */
fun sha3String(utf8String: String): String =
        toHexString(sha3(utf8String.toByteArray(StandardCharsets.UTF_8)))

/**
 * Generates SHA-256 digest for the given `input`.
 *
 * @param input The input to digest
 * @return The hash value for the given input
 * @throws RuntimeException If we couldn't find any SHA-256 provider
 */
@Throws(NoSuchAlgorithmException::class)
fun sha256(input: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256")
                .digest(input)