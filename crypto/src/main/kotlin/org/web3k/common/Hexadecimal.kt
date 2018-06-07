package org.web3k.common

/** Returns 2 char hex string for Byte */
fun Byte.toHexString(): String =
        toInt().let {
            CHARS[it.shr(4) and 0x0f].toString() + CHARS[it.and(0x0f)].toString()
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
