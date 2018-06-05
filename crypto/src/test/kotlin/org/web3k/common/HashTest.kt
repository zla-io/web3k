package org.web3k.common

import org.assertj.core.api.Assertions.assertThat

import org.junit.jupiter.api.Test

class HashTest {

    @Test
    fun testSha3() {
        val input = byteArrayOf(
                asByte(0x6, 0x8),
                asByte(0x6, 0x5),
                asByte(0x6, 0xc),
                asByte(0x6, 0xc),
                asByte(0x6, 0xf),
                asByte(0x2, 0x0),
                asByte(0x7, 0x7),
                asByte(0x6, 0xf),
                asByte(0x7, 0x2),
                asByte(0x6, 0xc),
                asByte(0x6, 0x4))

        val expected = byteArrayOf(
                asByte(0x4, 0x7),
                asByte(0x1, 0x7),
                asByte(0x3, 0x2),
                asByte(0x8, 0x5),
                asByte(0xa, 0x8),
                asByte(0xd, 0x7),
                asByte(0x3, 0x4),
                asByte(0x1, 0xe),
                asByte(0x5, 0xe),
                asByte(0x9, 0x7),
                asByte(0x2, 0xf),
                asByte(0xc, 0x6),
                asByte(0x7, 0x7),
                asByte(0x2, 0x8),
                asByte(0x6, 0x3),
                asByte(0x8, 0x4),
                asByte(0xf, 0x8),
                asByte(0x0, 0x2),
                asByte(0xf, 0x8),
                asByte(0xe, 0xf),
                asByte(0x4, 0x2),
                asByte(0xa, 0x5),
                asByte(0xe, 0xc),
                asByte(0x5, 0xf),
                asByte(0x0, 0x3),
                asByte(0xb, 0xb),
                asByte(0xf, 0xa),
                asByte(0x2, 0x5),
                asByte(0x4, 0xc),
                asByte(0xb, 0x0),
                asByte(0x1, 0xf),
                asByte(0xa, 0xd))

        val result = sha3(input)
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun testSha3HashHex() {
        assertThat(sha3(""))
                .isEqualTo("0xc5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470")

        assertThat(sha3("68656c6c6f20776f726c64"))
                .isEqualTo("0x47173285a8d7341e5e972fc677286384f802f8ef42a5ec5f03bbfa254cb01fad")
    }

    @Test
    fun testSha3String() {
        assertThat(sha3String(""))
                .isEqualTo("0xc5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470")

        assertThat(sha3String("EVWithdraw(address,uint256,bytes32)"))
                .isEqualTo("0x953d0c27f84a9649b0e121099ffa9aeb7ed83e65eaed41d3627f895790c72d41")
    }

    @Test
    fun testByte() {
        assertThat(asByte(0x0, 0x0)).isEqualTo(0x0.toByte())
        assertThat(asByte(0x1, 0x0)).isEqualTo(0x10.toByte())
        assertThat(asByte(0xf, 0xf)).isEqualTo(0xff.toByte())
        assertThat(asByte(0xc, 0x5)).isEqualTo(0xc5.toByte())
    }
}
