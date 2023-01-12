package it.zoo.vendro.internals.model

import it.zoo.vendro.racoon.internals.model.getValue
import it.zoo.vendro.racoon.internals.model.getValueK
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PropertyExtractorKtTest {
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