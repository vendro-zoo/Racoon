package internals.query

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class QueryProcessingTest {

    @Test
    fun reconstructQuery() {
        val reconstructQuery = QueryProcessing.reconstructQuery("SELECT * FROM `table` WHERE `id`=:id")

        assertEquals("SELECT * FROM `table` WHERE `id`=?", reconstructQuery.first)
        assertEquals(0, reconstructQuery.second.indexedParametersMappings.size)
        assertEquals(1, reconstructQuery.second.namedParametersMappings.size)
        assertEquals("id", reconstructQuery.second.namedParametersMappings.keys.first())
        assertEquals(1, reconstructQuery.second.namedParametersMappings.values.first())
    }

    @Test
    fun reconstructQueryWithMultipleParameters() {
        val reconstructQuery = QueryProcessing.reconstructQuery("SELECT * FROM `table` WHERE `id`=:id AND `name`=?")

        assertEquals("SELECT * FROM `table` WHERE `id`=? AND `name`=?", reconstructQuery.first)
        assertEquals(1, reconstructQuery.second.indexedParametersMappings.size)
        assertEquals(1, reconstructQuery.second.namedParametersMappings.size)
        assertEquals("id", reconstructQuery.second.namedParametersMappings.keys.first())
        assertEquals(1, reconstructQuery.second.namedParametersMappings.values.first())
        assertEquals(1, reconstructQuery.second.indexedParametersMappings.keys.first())
        assertEquals(2, reconstructQuery.second.indexedParametersMappings.values.first())
    }
}