package org.web3k.crypto

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.web3k.common.hexStringToByteArray
import java.security.SignatureException

class SignTest {

    companion object {
        private val TEST_MESSAGE = "A test message".toByteArray()
    }

    @Test
    fun testSignMessage() {
        val signatureData = signMessage(TEST_MESSAGE, KEY_PAIR)

        val expected = SignatureData(
                27.toByte(),
                hexStringToByteArray(
                        "0x9631f6d21dec448a213585a4a41a28ef3d4337548aa34734478b563036163786"),
                hexStringToByteArray(
                        "0x2ff816ee6bbb82719e983ecd8a33a4b45d32a4b58377ef1381163d75eedc900b")
        )

        assertThat(signatureData).isEqualTo(expected)
    }

    @Test
    @Throws(SignatureException::class)
    fun testSignedMessageToKey() {
        val signatureData = signMessage(TEST_MESSAGE, KEY_PAIR)
        val key = signedMessageToKey(TEST_MESSAGE, signatureData)
        assertThat(key).isEqualTo(PUBLIC_KEY)
    }

    @Test
    fun testPublicKeyFromPrivateKey() {
        assertThat(publicKeyFromPrivate(PRIVATE_KEY)).isEqualTo(PUBLIC_KEY)
    }

    @Test
    fun testInvalidSignatureData() {
        assertThatThrownBy {
            signedMessageToKey(TEST_MESSAGE, SignatureData(27.toByte(), byteArrayOf(1), byteArrayOf(0)))
        }.isInstanceOf(IllegalArgumentException::class.java)
    }
}
