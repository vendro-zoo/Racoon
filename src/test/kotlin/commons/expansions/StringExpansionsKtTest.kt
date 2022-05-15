package commons.expansions

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class StringExpansionsKtTest {

    @Test
    fun isInQuotes() {
        val s1 = "'hello'"
        val s2 = "say 'hello' 2.0"
        val s3 = "say \\'hello\\' 2.0"

        assertTrue(s1.isInQuotes(1))
        assertTrue(s1.isInQuotes(0))
        assertTrue(s2.isInQuotes(7))
        assertFalse(s2.isInQuotes(0))
        assertFalse(s3.isInQuotes(7))
        assertFalse(s3.isInQuotes(0))
    }

    @Test
    fun snakeCase() {
        assertEquals("123_hel_1_lo_123", "123hel1lo123".snakeCase())
        assertEquals("hello_World_123", "helloWorld123".snakeCase())
        assertEquals("0_Hello_123_World_4", "0Hello123World4".snakeCase())
    }

    @Test
    fun lowerSnakeCase() {
        assertEquals("hello_world_123", "helloWorld123".lowerSnakeCase())
        assertEquals("hello_world_123", "HelloWorld123".lowerSnakeCase())
    }

    @Test
    fun upperSnakeCase() {
        assertEquals("HELLO_WORLD_123", "helloWorld123".upperSnakeCase())
        assertEquals("HELLO_WORLD_123", "HelloWorld123".upperSnakeCase())
    }
}