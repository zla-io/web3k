package org.web3k.wallet

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

object BIP44Test : Spek({

    given("Bip44") {

        on("invalid inputs") {

            it("fails for invalid path") {
                assertThatThrownBy { "abc".toBip44() }
                        .isInstanceOf(IllegalArgumentException::class.java)
            }

            it("fails for empty path") {
                assertThatThrownBy { "".toBip44() }
                        .isInstanceOf(IllegalArgumentException::class.java)
            }

            it("fail for too short path") {
                assertThatThrownBy { "m".toBip44() }
                        .isInstanceOf(IllegalArgumentException::class.java)
            }
        }

        on("dirty paths") {

            for ((key, value) in dirtyPaths + dirtyPaths) {

                it("parses $key") {
                    assertThat(key.toBip44().path).isEqualTo(value)
                }
            }
        }


        for ((path, value) in samples) {
            val (elts, ints) = value

                on("valid path $path") {

                    it("resolve each numbers") {
                        assertThat(path.toBip44().toInts())
                                .isEqualTo(ints)
                    }

                    it("toString is returning path") {
                        assertThat(path)
                                .isEqualTo(Bip44(elts).toString())
                    }
                }
        }

        on("nextElement") {

            it("returns last element + 1") {
                assertThat("m/0/1/2".toBip44().nextElement())
                        .isEqualTo("m/0/1/3".toBip44())
            }
        }
    }
})

private val samples = mapOf(
        "m/0" to (
                listOf(BIP44Element(false, 0))
                        to intArrayOf(0)),
        "m/0'" to (
                listOf(BIP44Element(true, 0))
                        to intArrayOf(0x80000000.toInt())),
        "m/0/1" to (
                listOf(BIP44Element(false, 0), BIP44Element(false, 1))
                        to intArrayOf(0, 1)),
        "m/44'" to (
                listOf(BIP44Element(true, 44))
                        to intArrayOf(0x8000002C.toInt())),
        "m/44'/1" to (
                listOf(BIP44Element(true, 44), BIP44Element(false, 1))
                        to intArrayOf(0x8000002C.toInt(), 1))
)

private val dirtyPaths = mapOf(
        "m/44 ' " to listOf(BIP44Element(true, 44)),
        "m/0 /1 ' " to listOf(BIP44Element(false, 0), BIP44Element(true, 1))
)