package org.web3k.common

import org.bouncycastle.jcajce.provider.digest.Keccak

fun String.keccak(): ByteArray =
        hexToByteArray().keccak()

fun ByteArray.keccak(): ByteArray =
        Keccak.Digest256().let {
            it.update(this)
            it.digest()
        }
