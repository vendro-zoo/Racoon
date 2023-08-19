package it.zoo.vendro.racoon.habitat.statements

import it.zoo.vendro.racoon.TestConfiguration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class InsertStatementTest {
    val pool = TestConfiguration.POOL

    @Test
    fun executeBatch() {
        pool.getManager().use { rm ->
            val ir = rm.createInsert("INSERT INTO cat (name, age) VALUES (:name, :age)")

            ir.addBatch {
                it.setParam("name", "Bobby")
                it.setParam("age", 3)
            }.addBatch {
                it.setParam("name", "Charlie")
                it.setParam("age", 4)
            }

            ir.executeBatch()
            println(ir.generatedKeys)
            assertEquals(2, ir.generatedKeys.size)
        }
    }
}