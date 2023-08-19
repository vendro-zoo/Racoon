package it.zoo.vendro.racoon.internals.query

import it.zoo.vendro.racoon.habitat.configuration.RacoonConfiguration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class QueryHelperKtTest {
    val config = RacoonConfiguration(
        naming = RacoonConfiguration.Naming(
            tableNameMapper = { it },
        )
    )

    data class TestClass(val a: Int, val b: String)

    @Test
    fun generateInsertQueryK1() {
        val query = generateInsertQueryK(TestClass::class, config)
        assertEquals("INSERT INTO `TestClass` (`a`,`b`) VALUES (:a,:b)", query)
    }

    @Test
    fun generateUpdateQueryK() {
        val query = generateUpdateQueryK(TestClass::class, config)
        assertEquals("UPDATE `TestClass` SET `a`=:a,`b`=:b WHERE `id`=:id", query)
    }

    @Test
    fun generateSelectQueryK() {
        val query = generateSelectQueryK(TestClass::class, config)
        assertEquals("SELECT `a`,`b` FROM `TestClass` WHERE `id`=:id", query)
    }
}