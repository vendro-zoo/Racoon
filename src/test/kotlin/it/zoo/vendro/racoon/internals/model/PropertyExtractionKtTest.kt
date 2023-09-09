package it.zoo.vendro.racoon.internals.model

import it.zoo.vendro.racoon.internals.utils.getValue
import it.zoo.vendro.racoon.internals.utils.getValueK
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PropertyExtractionKtTest {
    data class TestClass(val a: Int, val b: String)

    @Test
    fun getValueKTest() {
        val test = TestClass(1, "test")
        assertEquals(1, getValueK(test, "a", TestClass::class))
        assertEquals("test", getValueK(test, "b", TestClass::class))
    }

    @Test
    fun getValueTest() {
        val test = TestClass(1, "test")
        assertEquals(1, getValue(test, "a"))
        assertEquals("test", getValue(test, "b"))
    }
}