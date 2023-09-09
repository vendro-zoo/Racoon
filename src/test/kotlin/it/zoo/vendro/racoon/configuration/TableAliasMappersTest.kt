package it.zoo.vendro.racoon.configuration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class TableAliasMapperTest {
    @Test
    fun onlyUpperToLower() {
        assertEquals("altn", RacoonConfiguration.Naming.onlyUpperToLower("ALongTableName"))
    }

    @Test
    fun onlyUpper() {
        assertEquals("ALTN", RacoonConfiguration.Naming.onlyUpper("ALongTableName"))
    }
}