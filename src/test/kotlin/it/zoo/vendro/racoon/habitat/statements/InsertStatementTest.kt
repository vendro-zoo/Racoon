package it.zoo.vendro.racoon.habitat.statements

import it.zoo.vendro.racoon.habitat.ConnectionPool
import it.zoo.vendro.racoon.habitat.configuration.RacoonConfiguration
import it.zoo.vendro.racoon.internals.configuration.ConnectionSettings
import it.zoo.vendro.racoon.internals.mappers.NameMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class InsertStatementTest {
    val pool = ConnectionPool()

    @BeforeEach
    fun setUp() {
        RacoonConfiguration.Connection.connectionSettings =
            ConnectionSettings(
                host = "localhost",
                port = 3306,
                database = "racoon-ktor-sample",
                username = "admin",
                password = "admin",
                idleTimeout = 3
            )
        RacoonConfiguration.Naming.tableNameMapper = NameMapper.lowerSnakeCase
        RacoonConfiguration.Naming.tableAliasMapper = NameMapper.lowerSnakeCase
    }

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