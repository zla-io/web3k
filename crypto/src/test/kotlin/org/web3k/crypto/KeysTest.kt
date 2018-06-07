/*
 * Created by Pierrick Greze on 2018-6-7.
 *
 * Copyright © 2018 ZLA - All rights reserved.
 */

package org.web3k.crypto

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.web3k.common.clean0xPrefix
import org.web3k.common.toBytesPadded
import org.web3k.common.toHexString
import org.web3k.random.secureRandom
import java.math.BigInteger

class KeysTest {

    @Test
    fun testCreateSecp256k1KeyPair() {
        val keyPair = createSecp256k1KeyPair()
        val privateKey = keyPair.private
        val publicKey = keyPair.public

        assertNotNull(privateKey)
        assertNotNull(publicKey)

        assertThat(privateKey.encoded.size).isEqualTo(144)
        assertThat(publicKey.encoded.size).isEqualTo(88)
    }

    @Test
    fun testCreateEcKeyPair() {
        val (privateKey, publicKey) = createEcKeyPair()
        assertThat(publicKey.signum()).isEqualTo(1)
        assertThat(privateKey.signum()).isEqualTo(1)
    }

    @Test
    fun testGetAddressString() {
        assertThat(PUBLIC_KEY_STRING.publicKeyToAddress()).isEqualTo(ADDRESS)
    }

    @Test
    fun testGetAddressZeroPaddedAddress() {
        val publicKey = "0xa1b31be4d58a7ddd24b135db0da56a90fb5382077ae26b250e1dc9cd6232ce22" + "70f4c995428bc76aa78e522316e95d7834d725efc9ca754d043233af6ca90113"
        assertThat(publicKey.publicKeyToAddress()).isEqualTo("0x01c52b08330e05d731e38c856c1043288f7d9744")
    }

    @Test
    fun testGetAddressBigInteger() {
        assertThat(PUBLIC_KEY.publicKeyToAddress()).isEqualTo(ADDRESS)
    }

    @Test
    fun testGetAddressSmallPublicKey() {
        assertThat("0x1234".publicKeyToAddress()).isEqualTo(PUBLIC_ADDRESS)
    }

    @Test
    fun testGetAddressZeroPadded() {
        val value = "1234"
        val publicKey = "0x" + "0".repeat(PUBLIC_KEY_LENGTH_IN_HEX - value.length) + value
        assertThat(publicKey.publicKeyToAddress()).isEqualTo(PUBLIC_ADDRESS)
    }
}

private val PUBLIC_ADDRESS = BigInteger.valueOf(0x1234)
        .toBytesPadded(PUBLIC_KEY_SIZE)
        .publicKeyToAddress()
        .toHexString()
