package org.web3k.common

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.math.BigInteger
import java.math.BigInteger.*

class BigIntegerTest {

    @Test
    fun paddingWorks() {
        assertThat(BigInteger("5").toBytesPadded(42).size).isEqualTo(42)
    }

    @Test
    fun maybeHexToBigIntegerWorks() {
        assertThat("0xa".maybeHexToBigInteger()).isEqualTo(TEN)
        assertThat("10".maybeHexToBigInteger()).isEqualTo(TEN)
        assertThat("0x0".maybeHexToBigInteger()).isEqualTo(ZERO)
        assertThat("0".maybeHexToBigInteger()).isEqualTo(ZERO)
        assertThat("0x1".maybeHexToBigInteger()).isEqualTo(ONE)
        assertThat("1".maybeHexToBigInteger()).isEqualTo(ONE)
        assertThat("1001".maybeHexToBigInteger()).isEqualTo(1001)

        assertThatThrownBy { "a".maybeHexToBigInteger() }
                .isInstanceOf(NumberFormatException::class.java)
        assertThatThrownBy { "0x?".maybeHexToBigInteger() }
                .isInstanceOf(NumberFormatException::class.java)
        assertThatThrownBy { "yolo".maybeHexToBigInteger() }
                .isInstanceOf(NumberFormatException::class.java)
    }
}
