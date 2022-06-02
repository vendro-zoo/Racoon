package internals.query

import habitat.configuration.RacoonConfiguration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

internal class QueryHelperKtTest {
    data class TestClass(val a: Int, val b: String)

    companion object {
        @JvmStatic
        @BeforeAll
        fun setUp() {
            RacoonConfiguration.Naming.setTableNameMapper { it }
        }
    }

    @Test
    fun generateInsertQueryK1() {
        val query = generateInsertQueryK(TestClass::class)
        assertEquals("INSERT INTO `TestClass` (`a`,`b`) VALUE (:a,:b)", query)
    }

    @Test
    fun generateUpdateQueryK() {
        val query = generateUpdateQueryK(TestClass::class)
        assertEquals("UPDATE `TestClass` SET `a`=:a,`b`=:b WHERE `id`=:id", query)
    }

    @Test
    fun generateSelectQueryK() {
        val query = generateSelectQueryK(TestClass::class)
        assertEquals("SELECT * FROM `TestClass` WHERE `id`=:id", query)
    }
}