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
        private val HEX_RANGE_ARRAY = byteArrayOf(
                asByte(0x0, 0x1),
                asByte(0x2, 0x3),
                asByte(0x4, 0x5),
                asByte(0x6, 0x7),
                asByte(0x8, 0x9),
                asByte(0xa, 0xb),
                asByte(0xc, 0xd),
                asByte(0xe, 0xf))

        private const val HEX_RANGE_STRING = "0x0123456789abcdef"
    }

    @Test
    fun testQuantityEncodeLeadingZero() {
        assertThat(toHexStringWithPrefixSafe(BigInteger.valueOf(0L))).isEqualTo("0x00")
        assertThat(toHexStringWithPrefixSafe(BigInteger.valueOf(1024L))).isEqualTo("0x400")
        assertThat(toHexStringWithPrefixSafe(BigInteger.valueOf(java.lang.Long.MAX_VALUE)))
                .isEqualTo("0x7fffffffffffffff")
        assertThat(toHexStringWithPrefixSafe(
                BigInteger("204516877000845695339750056077105398031")))
                .isEqualTo("0x99dc848b94efc27edfad28def049810f")
    }

    @Test
    fun testQuantityDecode() {
        assertThat(decodeQuantity("0x0")).isEqualTo(BigInteger.valueOf(0L))
        assertThat(decodeQuantity("0x400")).isEqualTo(BigInteger.valueOf(1024L))
        assertThat(decodeQuantity("0x0")).isEqualTo(BigInteger.valueOf(0L))
        assertThat(decodeQuantity(
                "0x7fffffffffffffff")).isEqualTo(BigInteger.valueOf(java.lang.Long.MAX_VALUE))
        assertThat(decodeQuantity("0x99dc848b94efc27edfad28def049810f"))
                .isEqualTo(BigInteger("204516877000845695339750056077105398031"))
    }

    @Test
    fun testQuantityDecodeLeadingZero() {
        assertThat(decodeQuantity("0x0400")).isEqualTo(BigInteger.valueOf(1024L))
        assertThat(decodeQuantity("0x001")).isEqualTo(BigInteger.valueOf(1L))
    }

    // If TestRpc resolves the following issue, we can reinstate this code
    // https://github.com/ethereumjs/testrpc/issues/220
    @Disabled
    @Test
    fun testQuantityDecodeLeadingZeroException() {
        assertThatThrownBy { decodeQuantity("0x0400") }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun testQuantityDecodeMissingPrefix() {
        assertThatThrownBy { decodeQuantity("ff") }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun testQuantityDecodeMissingValue() {
        assertThatThrownBy { decodeQuantity("0x") }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun testQuantityEncode() {
        assertThat(encodeQuantity(BigInteger.valueOf(0))).isEqualTo("0x0")
        assertThat(encodeQuantity(BigInteger.valueOf(1))).isEqualTo("0x1")
        assertThat(encodeQuantity(BigInteger.valueOf(1024))).isEqualTo("0x400")
        assertThat(encodeQuantity(BigInteger.valueOf(java.lang.Long.MAX_VALUE)))
                .isEqualTo("0x7fffffffffffffff")
        assertThat(encodeQuantity(BigInteger("204516877000845695339750056077105398031")))
                .isEqualTo("0x99dc848b94efc27edfad28def049810f")
    }

    @Test
    fun testQuantityEncodeNegative() {
        assertThatThrownBy { encodeQuantity(BigInteger.valueOf(-1)) }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun testCleanHexPrefix() {
        assertThat(cleanHexPrefix("")).isEqualTo("")
        assertThat(cleanHexPrefix("0123456789abcdef")).isEqualTo("0123456789abcdef")
        assertThat(cleanHexPrefix("0x")).isEqualTo("")
        assertThat(cleanHexPrefix("0x0123456789abcdef")).isEqualTo("0123456789abcdef")
    }

    @Test
    fun testPrependHexPrefix() {
        assertThat(prependHexPrefix("")).isEqualTo("0x")
        assertThat(prependHexPrefix("0x0123456789abcdef")).isEqualTo("0x0123456789abcdef")
        assertThat(prependHexPrefix("0x")).isEqualTo("0x")
        assertThat(prependHexPrefix("0123456789abcdef")).isEqualTo("0x0123456789abcdef")
    }

    @Test
    fun testToHexStringWithPrefix() {
        assertThat(toHexStringWithPrefix(BigInteger.TEN)).isEqualTo("0xa")
    }

    @Test
    fun testToHexStringNoPrefix() {
        assertThat(toHexStringNoPrefix(BigInteger.TEN)).isEqualTo("a")
    }

    @Test
    fun testToBytesPadded() {
        assertThat(toBytesPadded(BigInteger.TEN, 1))
                .isEqualTo(byteArrayOf(0xa))

        assertThat(toBytesPadded(BigInteger.TEN, 8))
                .isEqualTo(byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0xa))

        assertThat(toBytesPadded(BigInteger.valueOf(Integer.MAX_VALUE.toLong()), 4))
                .isEqualTo(byteArrayOf(0x7f, 0xff.toByte(), 0xff.toByte(), 0xff.toByte()))
    }

    @Test
    fun testToBytesPaddedInvalid() {
        assertThatThrownBy { toBytesPadded(BigInteger.valueOf(java.lang.Long.MAX_VALUE), 7) }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun testHexStringToByteArray() {
        assertThat(hexStringToByteArray("")).isEqualTo(byteArrayOf())
        assertThat(hexStringToByteArray("0")).isEqualTo(byteArrayOf(0))
        assertThat(hexStringToByteArray("1")).isEqualTo(byteArrayOf(0x1))
        assertThat(hexStringToByteArray(HEX_RANGE_STRING))
                .isEqualTo(HEX_RANGE_ARRAY)
        assertThat(hexStringToByteArray("0x123"))
                .isEqualTo(byteArrayOf(0x1, 0x23))
    }

    @Test
    fun testToHexString() {
        assertThat(toHexString(byteArrayOf())).isEqualTo("0x")
        assertThat(toHexString(byteArrayOf(0x1))).isEqualTo("0x01")
        assertThat(toHexString(HEX_RANGE_ARRAY)).isEqualTo(HEX_RANGE_STRING)
    }

    @Test
    fun testToHexStringNoPrefixZeroPadded() {
        assertThat(toHexStringNoPrefixZeroPadded(BigInteger.ZERO, 5))
                .isEqualTo("00000")

        assertThat(toHexStringNoPrefixZeroPadded(BigInteger("11c52b08330e05d731e38c856c1043288f7d9744", 16), 40))
                .isEqualTo("11c52b08330e05d731e38c856c1043288f7d9744")

        assertThat(toHexStringNoPrefixZeroPadded(BigInteger("01c52b08330e05d731e38c856c1043288f7d9744", 16), 40))
                .isEqualTo("01c52b08330e05d731e38c856c1043288f7d9744")
    }

    @Test
    fun testToHexStringWithPrefixZeroPadded() {
        assertThat(toHexStringWithPrefixZeroPadded(BigInteger.ZERO, 5))
                .isEqualTo("0x00000")

        assertThat(toHexStringWithPrefixZeroPadded(BigInteger("01c52b08330e05d731e38c856c1043288f7d9744", 16), 40))
                .isEqualTo("0x01c52b08330e05d731e38c856c1043288f7d9744")

        assertThat(toHexStringWithPrefixZeroPadded(BigInteger("01c52b08330e05d731e38c856c1043288f7d9744", 16), 40))
                .isEqualTo("0x01c52b08330e05d731e38c856c1043288f7d9744")
    }

    @Test
    fun testToHexStringZeroPaddedNegative() {
        assertThatThrownBy { toHexStringNoPrefixZeroPadded(BigInteger.valueOf(-1), 20) }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun testToHexStringZeroPaddedTooLarge() {
        assertThatThrownBy { toHexStringNoPrefixZeroPadded(BigInteger.valueOf(100), 1) }
                .isInstanceOf(UnsupportedOperationException::class.java)
    }

    @Test
    fun testIsIntegerValue() {
        assertTrue(isIntegerValue(BigDecimal.ZERO))
        assertTrue(isIntegerValue(BigDecimal.ZERO))
        assertTrue(isIntegerValue(BigDecimal.valueOf(java.lang.Long.MAX_VALUE)))
        assertTrue(isIntegerValue(BigDecimal.valueOf(java.lang.Long.MIN_VALUE)))
        assertTrue(isIntegerValue(BigDecimal(
                "9999999999999999999999999999999999999999999999999999999999999999.0")))
        assertTrue(isIntegerValue(BigDecimal(
                "-9999999999999999999999999999999999999999999999999999999999999999.0")))

        assertFalse(isIntegerValue(BigDecimal.valueOf(0.1)))
        assertFalse(isIntegerValue(BigDecimal.valueOf(-0.1)))
        assertFalse(isIntegerValue(BigDecimal.valueOf(1.1)))
        assertFalse(isIntegerValue(BigDecimal.valueOf(-1.1)))
    }
}
