package it.zoo.vendro.internals.query

import it.zoo.vendro.racoon.habitat.configuration.RacoonConfiguration
import it.zoo.vendro.racoon.internals.query.generateInsertQueryK
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

internal class QueryHelperKtTest {
    data class TestClass(val a: Int, val b: String)

    companion object {
        @JvmStatic
        @BeforeAll
        fun setUp() {
            RacoonConfiguration.Naming.tableNameMapper = { it }
        }
    }

    @Test
    fun generateInsertQueryK1() {
        val query = generateInsertQueryK(TestClass::class)
        assertEquals("INSERT INTO `TestClass` (`a`,`b`) VALUE (:a,:b)", query)
    }

    @Test
    fun generateUpdateQueryK() {
        val query = it.zoo.vendro.racoon.internals.query.generateUpdateQueryK(TestClass::class)
        assertEquals("UPDATE `TestClass` SET `a`=:a,`b`=:b WHERE `id`=:id", query)
    }

    @Test
    fun generateSelectQueryK() {
        val query = it.zoo.vendro.racoon.internals.query.generateSelectQueryK(TestClass::class)
        assertEquals("SELECT `a`,`b` FROM `TestClass` WHERE `id`=:id", query)
    }
}