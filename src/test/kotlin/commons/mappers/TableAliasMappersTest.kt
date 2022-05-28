package commons.mappers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class TableAliasMappersTest {
    @Test
    fun onlyUpperToLower() {
        assertEquals("altn", TableAliasMappers.onlyUpperToLower("ALongTableName"))
    }

    @Test
    fun onlyUpper() {
        assertEquals("ALTN", TableAliasMappers.onlyUpper("ALongTableName"))
    }
}