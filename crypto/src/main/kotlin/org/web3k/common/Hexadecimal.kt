package org.web3k.common

import kotlin.experimental.and

fun ByteArray.toHexString(withPrefix: Boolean = true): String {
    val stringBuilder = StringBuilder()
    if (withPrefix) {
        stringBuilder.append("0x")
    }
    for (i in 0 until size) {
        stringBuilder.append(String.format("%02x", this[i] and 0xFF.toByte()))
    }
    return stringBuilder.toString()
}

/** Returns 2 char hex string for Byte */
fun Byte.toHexString(): String =
        toInt().let {
            CHARS[it.shr(4) and 0x0f].toString() +
                    CHARS[it.and(0x0f)].toString()
        }

fun Char.fromHexToInt(): Int =
        CHARS.indexOf(this)

private const val CHARS = "0123456789abcdef"

fun ByteArray.toHexString(): String =
        joinToString("") { it.toHexString() }
                .prepend0xPrefix()

fun String.hexToByteArray(): ByteArray {
    require (length % 2 == 0) {
        "hex-string must have an even number of digits (nibbles)"
    }

    val cleanInput = clean0xPrefix()

    return ByteArray(cleanInput.length / 2).apply {
        var i = 0
        while (i < cleanInput.length) {
            this[i / 2] = ((cleanInput[i].getNibbleValue() shl 4) + cleanInput[i + 1].getNibbleValue()).toByte()
            i += 2
        }
    }
}

// TODO: What's different with hexToByteArray from kethereum?
fun String.hexStringToByteArray(): ByteArray {
    val cleanInput = clean0xPrefix()

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

private fun Char.getNibbleValue(): Int =
        Character.digit(this, 16)
                .also { char ->
                    require(char != -1) { "Not a valid hex char: $this" }
                }

fun String.has0xPrefix(): Boolean =
        startsWith("0x")

fun String.prepend0xPrefix(): String =
        if (has0xPrefix()) this else "0x$this"

fun String.clean0xPrefix(): String =
        if (has0xPrefix()) this.substring(2) else this
