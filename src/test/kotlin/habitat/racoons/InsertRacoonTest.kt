package habitat.racoons

import habitat.RacoonDen
import habitat.configuration.RacoonConfiguration
import internals.configuration.ConnectionSettings
import internals.mappers.NameMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class InsertRacoonTest {

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
        RacoonDen.getManager().use { rm ->
            val ir = rm.createInsertRacoon("INSERT INTO cat (name, age) VALUES (:name, :age)")

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