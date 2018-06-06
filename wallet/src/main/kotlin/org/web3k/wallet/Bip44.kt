package org.web3k.wallet

//
// BIP44 as in https://github.com/bitcoin/bips/blob/master/bip-0044.mediawiki
//

data class BIP44Element(
        val isHardened: Boolean,
        val number: Int
)

data class Bip44(val path: List<BIP44Element>) {

    override fun toString() =
            "m/" + path.joinToString("/") {
                "${it.number}" + if (it.isHardened) "'" else ""
            }
}

const val HARDENING_FLAG = 0x80000000.toInt()

val Int.isHardened: Boolean
    get() = (this and HARDENING_FLAG) != 0

/** Returns a [Bip44] instance from provided path. */
fun String.toBip44(): Bip44 {
    require(trim().startsWith("m/")) {
        "Must start with m/"
    }
    return Bip44(substring(2)
            .split("/")
            .mapNotNull { it.trim().takeIf { it.isNotEmpty() } }
            .map {
                BIP44Element(
                        isHardened = it.endsWith("'"),
                        number = it.replace("'", "").trim().toInt()
                )
            })
}

fun Bip44.nextElement(): Bip44 =
        Bip44(path.take(path.size - 1)
                + path.last().let { it.copy(number = it.number + 1) })

fun Bip44.toInts(): IntArray =
        path.map { element ->
            element.number
                    .takeUnless { element.isHardened }
                    ?: (element.number or HARDENING_FLAG)
        }.toIntArray()
