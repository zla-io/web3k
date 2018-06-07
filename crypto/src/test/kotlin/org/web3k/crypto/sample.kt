package org.web3k.crypto

import org.web3k.common.toBigInt

// Keys generated for unit testing purposes.

const val PRIVATE_KEY_STRING = "a392604efc2fad9c0b3da43b5f698a2e3f270f170d859912be0d54742275c5f6"
const val PUBLIC_KEY_STRING = "0x506bc1dc099358e5137292f4efdd57e400f29ba5132aa5d12b18dac1c1f6aaba645c0b7b58158babbfa6c6cd5a48aa7340a8749176b120e8516216787a13dc76"
const val ADDRESS = "0xef678007d18427e6022059dbc264f27507cd1ffc"

const val PASSWORD = "Insecure Pa55w0rd"

val PRIVATE_KEY = toBigInt(PRIVATE_KEY_STRING)
val PUBLIC_KEY = toBigInt(PUBLIC_KEY_STRING)

val KEY_PAIR = ECKeyPair(PRIVATE_KEY, PUBLIC_KEY)

//val CREDENTIALS = create(KEY_PAIR)
