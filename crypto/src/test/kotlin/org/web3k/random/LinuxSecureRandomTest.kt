package org.web3k.random

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.web3k.random.isAndroid
import org.web3k.random.secureRandom

class LinuxSecureRandomTest {

    @Test
    fun `secureRandom is working`() {
        secureRandom.nextInt()
    }

    @Test
    fun `not android runtime`() {
        assertFalse(isAndroid)
    }
}
