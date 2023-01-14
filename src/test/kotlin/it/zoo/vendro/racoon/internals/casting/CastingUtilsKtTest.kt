package it.zoo.vendro.racoon.internals.casting

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

        runCatching { castEquivalent(parameter, "test") }
            .fold(
                onSuccess = { fail("Should fail") },
                onFailure = { if (it.message != "Cannot cast test from String to Int") fail("Wrong error message") }
            )

        castEquivalent(parameter, value)
        castEquivalent(parameter, value.toLong())
        castEquivalent(parameter, value.toShort())
        castEquivalent(parameter, value.toByte())
        castEquivalent(parameter, value.toFloat())
        castEquivalent(parameter, value.toDouble())

        castEquivalent(parameter, value.toUInt())
        castEquivalent(parameter, value.toULong())
        castEquivalent(parameter, value.toUShort())
        castEquivalent(parameter, value.toUByte())
    }

    @Test
    fun castEquivalent2() {
        val parameter = TestClass2::class.primaryConstructor!!.parameters.first()
        val value = 1

        runCatching { castEquivalent(parameter, "test") }
            .fold(
                onSuccess = { fail("Should fail") },
                onFailure = { if (it.message != "Cannot cast test from String to UInt") fail("Wrong error message") }
            )

        castEquivalent(parameter, value)
        castEquivalent(parameter, value.toLong())
        castEquivalent(parameter, value.toShort())
        castEquivalent(parameter, value.toByte())
        castEquivalent(parameter, value.toFloat())
        castEquivalent(parameter, value.toDouble())

        castEquivalent(parameter, value.toUInt())
        castEquivalent(parameter, value.toULong())
        castEquivalent(parameter, value.toUShort())
        castEquivalent(parameter, value.toUByte())
    }
}