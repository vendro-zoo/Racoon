package it.zoo.vendro.internals.mappers

import it.zoo.vendro.racoon.internals.mappers.TableAliasMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class TableAliasMapperTest {
    @Test
    fun onlyUpperToLower() {
        assertEquals("altn", TableAliasMapper.onlyUpperToLower("ALongTableName"))
    }

    @Test
    fun onlyUpper() {
        assertEquals("ALTN", TableAliasMapper.onlyUpper("ALongTableName"))
    }
}