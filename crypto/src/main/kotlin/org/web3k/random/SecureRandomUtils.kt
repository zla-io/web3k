package org.web3k.random

import java.security.SecureRandom

/**
 * Provides an always secure [SecureRandom] instance.
 *
 * This is to address issues with SecureRandom on Android.
 * For more information, refer to the following [issue](https://github.com/web3j/web3j/issues/146).
 */
val secureRandom: SecureRandom by lazy {
    if (isAndroid) {
        overrideSecureRandomForUnix()
    }
    SecureRandom()
}

/**
 * Taken from [BitcoinJ implementation](https://github.com/bitcoinj/bitcoinj/blob/3cb1f6c6c589f84fe6e1fb56bf26d94cccc85429/core/src/main/java/org/bitcoinj/core/Utils.java#L573)
 */
internal val isAndroid: Boolean by lazy {
    System.getProperty("java.runtime.name") == "Android Runtime"
}
