package org.web3k.common

import java.math.BigDecimal
import java.math.BigInteger
import java.util.*
import kotlin.experimental.and

private const val HEX_PREFIX = "0x"

fun encodeQuantity(value: BigInteger): String {
    return if (value.signum() != -1) {
        HEX_PREFIX + value.toString(16)
    } else {
        throw IllegalArgumentException("Negative values are not supported")
    }
}

fun decodeQuantity(value: String): BigInteger {
    if (!isValidHexQuantity(value)) {
        throw IllegalArgumentException("Value must be in format 0x[1-9]+[0-9]* or 0x0")
    }
    try {
        return BigInteger(value.substring(2), 16)
    } catch (e: NumberFormatException) {
        throw IllegalArgumentException("Negative ", e)
    }
}

// If TestRpc resolves the following issue, we can reinstate this code
// https://github.com/ethereumjs/testrpc/issues/220
// if (value.length() > 3 && value.charAt(2) == '0') {
//    return false;
// }
private fun isValidHexQuantity(value: String): Boolean =
        value.length >= 3 && value.startsWith(HEX_PREFIX)

fun cleanHexPrefix(input: String): String =
        if (containsHexPrefix(input)) {
            input.substring(2)
        } else {
            input
        }

fun prependHexPrefix(input: String): String =
        if (!containsHexPrefix(input)) {
            HEX_PREFIX + input
        } else {
            input
        }

fun containsHexPrefix(input: String): Boolean =
        input.length > 1 && input[0] == '0' && input[1] == 'x'

fun toBigInt(value: ByteArray, offset: Int, length: Int): BigInteger =
        toBigInt(Arrays.copyOfRange(value, offset, offset + length))

fun toBigInt(value: ByteArray): BigInteger =
        BigInteger(1, value)

fun toBigInt(hexValue: String): BigInteger =
        toBigIntNoPrefix(cleanHexPrefix(hexValue))

fun toBigIntNoPrefix(hexValue: String): BigInteger =
        BigInteger(hexValue, 16)

fun toHexStringWithPrefix(value: BigInteger): String =
        HEX_PREFIX + value.toString(16)

fun toHexStringNoPrefix(value: BigInteger): String =
        value.toString(16)

fun toHexStringNoPrefix(input: ByteArray): String =
        toHexString(input, 0, input.size, false)

fun toHexStringWithPrefixZeroPadded(value: BigInteger, size: Int): String =
        toHexStringZeroPadded(value, size, true)

fun toHexStringWithPrefixSafe(value: BigInteger): String =
        HEX_PREFIX + toHexStringNoPrefix(value).padStart(2, '0')

fun toHexStringNoPrefixZeroPadded(value: BigInteger, size: Int): String =
        toHexStringZeroPadded(value, size, false)

private fun toHexStringZeroPadded(value: BigInteger, size: Int, withPrefix: Boolean = false): String {
    var result = toHexStringNoPrefix(value)

    val length = result.length
    if (length > size) {
        throw UnsupportedOperationException("Value " + result + "is larger then length " + size)
    } else if (value.signum() < 0) {
        throw IllegalArgumentException("Value cannot be negative")
    }

    if (length < size) {
        result = "0".repeat(size - length) + result
    }

    return if (withPrefix) {
        HEX_PREFIX + result
    } else {
        result
    }
}

fun toBytesPadded(value: BigInteger, length: Int): ByteArray {
    val result = ByteArray(length)
    val bytes = value.toByteArray()

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

fun hexStringToByteArray(input: String): ByteArray {
    val cleanInput = cleanHexPrefix(input)

    val len = cleanInput.length

    if (len == 0) {
        return byteArrayOf()
    }

    val data: ByteArray
    val startIdx: Int
    if (len % 2 != 0) {
        data = ByteArray(len / 2 + 1)
        data[0] = Character.digit(cleanInput[0], 16).toByte()
        startIdx = 1
    } else {
        data = ByteArray(len / 2)
        startIdx = 0
    }

    var i = startIdx
    while (i < len) {
        data[(i + 1) / 2] = ((Character.digit(cleanInput[i], 16) shl 4) + Character.digit(cleanInput[i + 1], 16)).toByte()
        i += 2
    }
    return data
}

fun toHexString(input: ByteArray, offset: Int, length: Int, withPrefix: Boolean): String {
    val stringBuilder = StringBuilder()
    if (withPrefix) {
        stringBuilder.append("0x")
    }
    for (i in offset until offset + length) {
        stringBuilder.append(String.format("%02x", input[i] and 0xFF.toByte()))
    }
    return stringBuilder.toString()
}

fun toHexString(input: ByteArray): String =
        toHexString(input, 0, input.size, true)

fun asByte(m: Int, n: Int): Byte =
        (m shl 4 or n).toByte()

fun isIntegerValue(value: BigDecimal): Boolean =
        value.signum() == 0
                || value.scale() <= 0
                || value.stripTrailingZeros().scale() <= 0