package org.web3k.common

import java.math.BigDecimal
import java.math.BigInteger
import java.util.*

private const val HEX_PREFIX = "0x"

// Quantity

fun BigInteger.encodeQuantity(): String {
    return if (signum() != -1) {
        HEX_PREFIX + toString(16)
    } else {
        throw IllegalArgumentException("Negative values are not supported")
    }
}

fun String.decodeQuantity(): BigInteger {
    if (!isValidHexQuantity()) {
        throw IllegalArgumentException("Value must be in format 0x[1-9]+[0-9]* or 0x0")
    }
    try {
        return BigInteger(substring(2), 16)
    } catch (e: NumberFormatException) {
        throw IllegalArgumentException("Negative ", e)
    }
}

// If TestRpc resolves the following issue, we can reinstate this code
// https://github.com/ethereumjs/testrpc/issues/220
// if (value.length() > 3 && value.charAt(2) == '0') {
//    return false;
// }
private fun String.isValidHexQuantity(): Boolean =
        length >= 3 && startsWith(HEX_PREFIX)

// X to BigInteger

/** Equivalent of bytes.toBigInteger(0, bytes.size) */
fun ByteArray.toBigInteger(): BigInteger =
        BigInteger(1, this)

/** Convert only a part of provided bytes to a big integer. */
fun ByteArray.toBigInteger(offset: Int, length: Int): BigInteger =
        Arrays.copyOfRange(this, offset, offset + length)
                .toBigInteger()

fun String.hexToBigInteger(): BigInteger =
        BigInteger(clean0xPrefix(), 16)

// BigInteger to X

fun BigDecimal.isIntegerValue(): Boolean =
        signum() == 0
                || scale() <= 0
                || stripTrailingZeros().scale() <= 0

fun BigInteger.toHexStringWithPrefixSafe(): String =
        HEX_PREFIX + toHexStringNoPrefix().padStart(2, '0')

fun BigInteger.toHexStringNoPrefix(): String =
        toString(16)

fun BigInteger.toHexStringZeroPadded(size: Int, withPrefix: Boolean = true): String {
    var result = toHexStringNoPrefix()

    val length = result.length
    if (length > size) {
        throw UnsupportedOperationException("Value $result is larger then length $size")
    } else if (signum() < 0) {
        throw UnsupportedOperationException("Value cannot be negative")
    }

    if (length < size) {
        result = "0".repeat(size - length) + result
    }

    return if (withPrefix) {
        "0x$result"
    } else {
        result
    }
}

fun BigInteger.toBytesPadded(length: Int): ByteArray {
    val result = ByteArray(length)
    val bytes = toByteArray()

    val bytesLength: Int
    val srcOffset: Int
    if (bytes[0].toInt() == 0) {
        bytesLength = bytes.size - 1
        srcOffset = 1
    } else {
        bytesLength = bytes.size
        srcOffset = 0
    }

    if (bytesLength > length) {
        throw IllegalArgumentException("Input is too large to put in byte array of size $length")
    }

    val destOffset = length - bytesLength
    System.arraycopy(bytes, srcOffset, result, destOffset, bytesLength)
    return result
}

fun asByte(m: Int, n: Int): Byte =
        (m shl 4 or n).toByte()
