package it.zoo.vendro.racoon.internals.serdes

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.reflect.full.primaryConstructor

internal class CastingUtilsKtTest {

    class TestClass(val value: Int)
    class TestClass2(val value: UInt)

    @Test
    fun castEquivalent() {
        val parameter = TestClass::class.primaryConstructor!!.parameters.first()
        val value = 1

        runCatching { it.zoo.vendro.racoon.serdes.castEquivalent(parameter, "test") }
            .fold(
                onSuccess = { fail("Should fail") },
                onFailure = { if (it.message != "Cannot cast test from String to Int") fail("Wrong error message") }
            )

        it.zoo.vendro.racoon.serdes.castEquivalent(parameter, value)
        it.zoo.vendro.racoon.serdes.castEquivalent(parameter, value.toLong())
        it.zoo.vendro.racoon.serdes.castEquivalent(parameter, value.toShort())
        it.zoo.vendro.racoon.serdes.castEquivalent(parameter, value.toByte())
        it.zoo.vendro.racoon.serdes.castEquivalent(parameter, value.toFloat())
        it.zoo.vendro.racoon.serdes.castEquivalent(parameter, value.toDouble())

        it.zoo.vendro.racoon.serdes.castEquivalent(parameter, value.toUInt())
        it.zoo.vendro.racoon.serdes.castEquivalent(parameter, value.toULong())
        it.zoo.vendro.racoon.serdes.castEquivalent(parameter, value.toUShort())
        it.zoo.vendro.racoon.serdes.castEquivalent(parameter, value.toUByte())
    }

    @Test
    fun castEquivalent2() {
        val parameter = TestClass2::class.primaryConstructor!!.parameters.first()
        val value = 1

        runCatching { it.zoo.vendro.racoon.serdes.castEquivalent(parameter, "test") }
            .fold(
                onSuccess = { fail("Should fail") },
                onFailure = { if (it.message != "Cannot cast test from String to UInt") fail("Wrong error message") }
            )

        it.zoo.vendro.racoon.serdes.castEquivalent(parameter, value)
        it.zoo.vendro.racoon.serdes.castEquivalent(parameter, value.toLong())
        it.zoo.vendro.racoon.serdes.castEquivalent(parameter, value.toShort())
        it.zoo.vendro.racoon.serdes.castEquivalent(parameter, value.toByte())
        it.zoo.vendro.racoon.serdes.castEquivalent(parameter, value.toFloat())
        it.zoo.vendro.racoon.serdes.castEquivalent(parameter, value.toDouble())

        it.zoo.vendro.racoon.serdes.castEquivalent(parameter, value.toUInt())
        it.zoo.vendro.racoon.serdes.castEquivalent(parameter, value.toULong())
        it.zoo.vendro.racoon.serdes.castEquivalent(parameter, value.toUShort())
        it.zoo.vendro.racoon.serdes.castEquivalent(parameter, value.toUByte())
    }
}