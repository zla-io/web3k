package org.web3k.common

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

private val HEX_REGEX = Regex("0[xX][0-9a-fA-F]+")

class HexadecimalTest {

    @Test
    fun testToHexString() {
        assertThat(byteArrayOf().toHexString())
                .isEqualTo("0x")
        assertThat(byteArrayOf(0x1).toHexString())
                .isEqualTo("0x01")
        assertThat(NumericTest.HEX_RANGE_ARRAY.toHexString())
                .isEqualTo(NumericTest.HEX_RANGE_STRING)
    }

    @Test
    fun weCanProduceSingleDigitHex() {
        assertThat(0.toByte().toHexString()).isEqualTo("00")
        assertThat(1.toByte().toHexString()).isEqualTo("01")
        assertThat(15.toByte().toHexString()).isEqualTo("0f")
    }

    @Test
    fun weCanProduceDoubleDigitHex() {
        assertThat(16.toByte().toHexString()).isEqualTo("10")
        assertThat(42.toByte().toHexString()).isEqualTo("2a")
        assertThat(255.toByte().toHexString()).isEqualTo("ff")
    }

    @Test
    fun prefixIsIgnored() {
        assertThat("0xab".hexToByteArray()).isEqualTo("ab".hexToByteArray())
    }

    @Test
    fun sizesAreOk() {
        assertThat("0x".hexToByteArray()).hasSize(0)
        assertThat("ff".hexToByteArray()).hasSize(1)
        assertThat("ffaa".hexToByteArray()).hasSize(2)
        assertThat("ffaabb".hexToByteArray()).hasSize(3)
        assertThat("ffaabb44".hexToByteArray()).hasSize(4)
        assertThat("0xffaabb4455".hexToByteArray()).hasSize(5)
        assertThat("0xffaabb445566".hexToByteArray()).hasSize(6)
        assertThat("ffaabb44556677".hexToByteArray()).hasSize(7)
    }

    @Test
    fun exceptionOnOddInput() {
        assertThatThrownBy { "0xa".hexToByteArray() }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun testRoundTrip() {
        assertThat("00".hexToByteArray().toHexString()).isEqualTo("0x00")
        assertThat("ff".hexToByteArray().toHexString()).isEqualTo("0xff")
        assertThat("abcdef".hexToByteArray().toHexString()).isEqualTo("0xabcdef")
        assertThat("0xaa12456789bb".hexToByteArray().toHexString()).isEqualTo("0xaa12456789bb")
    }

    @Test
    fun regexMatchesForHEX() {
        assertThat(HEX_REGEX.matches("0x00")).isTrue()
        assertThat(HEX_REGEX.matches("0xabcdef123456")).isTrue()
    }

    @Test
    fun regexFailsForNonHEX() {
        assertThat(HEX_REGEX.matches("q")).isFalse()
        assertThat(HEX_REGEX.matches("")).isFalse()
        assertThat(HEX_REGEX.matches("0x+")).isFalse()
        assertThat(HEX_REGEX.matches("0xgg")).isFalse()
    }


    @Test
    fun detect0xWorks() {
        assertThat("2".has0xPrefix()).isEqualTo(false)
        assertThat("0xFF".has0xPrefix()).isEqualTo(true)
    }

    @Test
    fun prepend0xWorks() {
        assertThat("2".prepend0xPrefix()).isEqualTo("0x2")
        assertThat("0xFF".prepend0xPrefix()).isEqualTo("0xFF")
    }

    @Test
    fun clean0xWorks() {
        assertThat("2".clean0xPrefix()).isEqualTo("2")
        assertThat("0xFF".clean0xPrefix()).isEqualTo("FF")
    }

    @Test
    fun detectsInvalidHex() {
        assertThatThrownBy { "0xxx".hexToByteArray() }
                .isInstanceOf(IllegalArgumentException::class.java)
    }
}