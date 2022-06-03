package internals.query

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class QueryProcessingTest {

    @Test
    fun reconstructQuery() {
        val reconstructQuery = QueryProcessing.reconstructQuery("SELECT * FROM `table` WHERE `id`=:id")

        assertEquals("SELECT * FROM `table` WHERE `id`=?", reconstructQuery.first)
        assertEquals(0, reconstructQuery.second.size)
        assertEquals(1, reconstructQuery.third.size)
        assertEquals("id", reconstructQuery.third.keys.first())
        assertEquals(1, reconstructQuery.third.values.first())
    }

    @Test
    fun reconstructQueryWithMultipleParameters() {
        val reconstructQuery = QueryProcessing.reconstructQuery("SELECT * FROM `table` WHERE `id`=:id AND `name`=?")

        assertEquals("SELECT * FROM `table` WHERE `id`=? AND `name`=?", reconstructQuery.first)
        assertEquals(1, reconstructQuery.second.size)
        assertEquals(1, reconstructQuery.third.size)
        assertEquals("id", reconstructQuery.third.keys.first())
        assertEquals(1, reconstructQuery.third.values.first())
        assertEquals(1, reconstructQuery.second.keys.first())
        assertEquals(2, reconstructQuery.second.values.first())
    }
}