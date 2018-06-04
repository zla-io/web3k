/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.web3k.random

import java.io.*
import java.security.Provider
import java.security.SecureRandomSpi
import java.security.Security

/**
 * @see LinuxSecureRandom
 */
fun overrideSecureRandomForUnix() {
    LinuxSecureRandom()
}

/**
 * Implementation from
 * [BitcoinJ implementation](https://github.com/bitcoinj/bitcoinj/blob/master/core/src/main/java/org/bitcoinj/crypto/LinuxSecureRandom.java)
 *
 * A SecureRandom implementation that is able to override the standard JVM provided
 * implementation, and which simply serves random numbers by reading /dev/urandom. That is, it
 * delegates to the kernel on UNIX systems and is unusable on other platforms. Attempts to manually
 * set the seed are ignored. There is no difference between seed bytes and non-seed bytes, they are
 * all from the same source.
 */
class LinuxSecureRandom : SecureRandomSpi() {

    // DataInputStream is not thread safe, so each random object has its own.
    private val dis: DataInputStream = DataInputStream(urandom)

    override fun engineSetSeed(bytes: ByteArray) {
        // Ignore.
    }

    @Throws(IOException::class)
    override fun engineNextBytes(bytes: ByteArray) {
        dis.readFully(bytes) // This will block until all the bytes can be read.
    }

    override fun engineGenerateSeed(i: Int): ByteArray =
            ByteArray(i).also {
                engineNextBytes(it)
            }
}

@Suppress("DEPRECATION")
private class LinuxSecureRandomProvider : Provider(
        "LinuxSecureRandom",
        1.0,
        "A Linux specific random number provider that uses /dev/urandom"
) {
    init {
        put("SecureRandom.LinuxSecureRandom", LinuxSecureRandom::class.java.name)
    }
}

/** This stream is deliberately leaked. */
private val urandom: FileInputStream = FileInputStream(File("/dev/urandom")).apply {
    try {
        if (read() == -1) {
            throw IOException("/dev/urandom not readable?")
        }
        // Now override the default SecureRandom implementation with this one.
        val position = Security.insertProviderAt(LinuxSecureRandomProvider(), 1)
        if (position != -1) {
            println("Secure randomness will be read from /dev/urandom only.")
        } else {
            println("Randomness is already secure.")
        }
    } catch (e: FileNotFoundException) {
        // Should never happen.
        println("/dev/urandom does not appear to exist or is not openable")
        throw e
    } catch (e: IOException) {
        println("/dev/urandom does not appear to be readable")
        throw e
    }
}
