package org.web3k.common

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.math.BigInteger

class NumericTest {

    companion object {
        val HEX_RANGE_ARRAY = byteArrayOf(
                asByte(0x0, 0x1),
                asByte(0x2, 0x3),
                asByte(0x4, 0x5),
                asByte(0x6, 0x7),
                asByte(0x8, 0x9),
                asByte(0xa, 0xb),
                asByte(0xc, 0xd),
                asByte(0xe, 0xf))

        const val HEX_RANGE_STRING = "0x0123456789abcdef"
    }

    @Test
    fun testQuantityEncodeLeadingZero() {
        assertThat(BigInteger.valueOf(0L).toHexStringWithPrefixSafe())
                .isEqualTo("0x00")
        assertThat(BigInteger.valueOf(1024L).toHexStringWithPrefixSafe())
                .isEqualTo("0x400")
        assertThat(BigInteger.valueOf(java.lang.Long.MAX_VALUE).toHexStringWithPrefixSafe())
                .isEqualTo("0x7fffffffffffffff")
        assertThat(BigInteger("204516877000845695339750056077105398031").toHexStringWithPrefixSafe())
                .isEqualTo("0x99dc848b94efc27edfad28def049810f")
    }

    @Test
    fun testQuantityDecode() {
        assertThat("0x0".decodeQuantity())
                .isEqualTo(BigInteger.valueOf(0L))
        assertThat("0x400".decodeQuantity())
                .isEqualTo(BigInteger.valueOf(1024L))
        assertThat("0x0".decodeQuantity())
                .isEqualTo(BigInteger.valueOf(0L))
        assertThat("0x7fffffffffffffff".decodeQuantity())
                .isEqualTo(BigInteger.valueOf(java.lang.Long.MAX_VALUE))
        assertThat("0x99dc848b94efc27edfad28def049810f".decodeQuantity())
                .isEqualTo(BigInteger("204516877000845695339750056077105398031"))
    }

    @Test
    fun testQuantityDecodeLeadingZero() {
        assertThat("0x0400".decodeQuantity()).isEqualTo(BigInteger.valueOf(1024L))
        assertThat("0x001".decodeQuantity()).isEqualTo(BigInteger.valueOf(1L))
    }

    // If TestRpc resolves the following issue, we can reinstate this code
    // https://github.com/ethereumjs/testrpc/issues/220
    @Disabled
    @Test
    fun testQuantityDecodeLeadingZeroException() {
        assertThatThrownBy { "0x0400".decodeQuantity() }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun testQuantityDecodeMissingPrefix() {
        assertThatThrownBy { "ff".decodeQuantity() }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun testQuantityDecodeMissingValue() {
        assertThatThrownBy { "0x".decodeQuantity() }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun testQuantityEncode() {
        assertThat(BigInteger.valueOf(0).encodeQuantity()).isEqualTo("0x0")
        assertThat(BigInteger.valueOf(1).encodeQuantity()).isEqualTo("0x1")
        assertThat(BigInteger.valueOf(1024).encodeQuantity()).isEqualTo("0x400")
        assertThat(BigInteger.valueOf(java.lang.Long.MAX_VALUE).encodeQuantity())
                .isEqualTo("0x7fffffffffffffff")
        assertThat(BigInteger("204516877000845695339750056077105398031").encodeQuantity())
                .isEqualTo("0x99dc848b94efc27edfad28def049810f")
    }

    @Test
    fun testQuantityEncodeNegative() {
        assertThatThrownBy { BigInteger.valueOf(-1).encodeQuantity() }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun testToBytesPadded() {
        assertThat(BigInteger.TEN.toBytesPadded(1))
                .isEqualTo(byteArrayOf(0xa))

        assertThat(BigInteger.TEN.toBytesPadded(8))
                .isEqualTo(byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0xa))

        assertThat(BigInteger.valueOf(Integer.MAX_VALUE.toLong()).toBytesPadded(4))
                .isEqualTo(byteArrayOf(0x7f, 0xff.toByte(), 0xff.toByte(), 0xff.toByte()))
    }

    @Test
    fun testToBytesPaddedInvalid() {
        assertThatThrownBy { BigInteger.valueOf(java.lang.Long.MAX_VALUE).toBytesPadded(7) }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun testHexStringToByteArray() {
        assertThat("".hexStringToByteArray())
                .isEqualTo(byteArrayOf())

        assertThat("0".hexStringToByteArray())
                .isEqualTo(byteArrayOf(0))

        assertThat("1".hexStringToByteArray())
                .isEqualTo(byteArrayOf(0x1))

        assertThat(HEX_RANGE_STRING.hexStringToByteArray())
                .isEqualTo(HEX_RANGE_ARRAY)

        assertThat("0x123".hexStringToByteArray())
                .isEqualTo(byteArrayOf(0x1, 0x23))
    }

    @Test
    fun testToHexStringNoPrefixZeroPadded() {
        assertThat(BigInteger.ZERO.toHexStringZeroPadded(5, withPrefix = false))
                .isEqualTo("00000")

        assertThat(BigInteger("11c52b08330e05d731e38c856c1043288f7d9744", 16).toHexStringZeroPadded(40, withPrefix = false))
                .isEqualTo("11c52b08330e05d731e38c856c1043288f7d9744")

        assertThat(BigInteger("01c52b08330e05d731e38c856c1043288f7d9744", 16).toHexStringZeroPadded(40, withPrefix = false))
                .isEqualTo("01c52b08330e05d731e38c856c1043288f7d9744")
    }

    @Test
    fun testToHexStringZeroPadded() {
        assertThat(BigInteger.ZERO.toHexStringZeroPadded(5))
                .isEqualTo("0x00000")

        assertThat(BigInteger("01c52b08330e05d731e38c856c1043288f7d9744", 16).toHexStringZeroPadded(40))
                .isEqualTo("0x01c52b08330e05d731e38c856c1043288f7d9744")

        assertThat(BigInteger("01c52b08330e05d731e38c856c1043288f7d9744", 16).toHexStringZeroPadded(40))
                .isEqualTo("0x01c52b08330e05d731e38c856c1043288f7d9744")
    }

    @Test
    fun testToHexStringZeroPaddedNegative() {
        assertThatThrownBy { BigInteger.valueOf(-1).toHexStringZeroPadded(20) }
                .isInstanceOf(UnsupportedOperationException::class.java)
    }

    @Test
    fun testToHexStringZeroPaddedTooLarge() {
        assertThatThrownBy { BigInteger.valueOf(100).toHexStringZeroPadded(1) }
                .isInstanceOf(UnsupportedOperationException::class.java)
    }

    @Test
    fun testIsIntegerValue() {
        assertTrue(BigDecimal.ZERO.isIntegerValue())
        assertTrue(BigDecimal.ZERO.isIntegerValue())
        assertTrue(BigDecimal.valueOf(java.lang.Long.MAX_VALUE).isIntegerValue())
        assertTrue(BigDecimal.valueOf(java.lang.Long.MIN_VALUE).isIntegerValue())
        assertTrue(BigDecimal("9999999999999999999999999999999999999999999999999999999999999999.0").isIntegerValue())
        assertTrue(BigDecimal("-9999999999999999999999999999999999999999999999999999999999999999.0").isIntegerValue())

        assertFalse(BigDecimal.valueOf(0.1).isIntegerValue())
        assertFalse(BigDecimal.valueOf(-0.1).isIntegerValue())
        assertFalse(BigDecimal.valueOf(1.1).isIntegerValue())
        assertFalse(BigDecimal.valueOf(-1.1).isIntegerValue())
    }
}
