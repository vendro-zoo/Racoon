package internals.casting.builtin

import habitat.RacoonDen
import habitat.configuration.RacoonConfiguration
import internals.configuration.ConnectionSettings
import internals.mappers.NameMapper
import models.Dog
import models.DogColor
import models.DogSize
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class EnumCasterTest {
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
    }

    @Test
    fun enumInsert() {
        RacoonDen.getManager().use { rm ->
            rm.insert(
                Dog(
                    name = "Star",
                    size = DogSize.SMALL
                )
            )
        }
    }

    @Test
    fun enumInsertWithColumnName() {
        RacoonDen.getManager().use { rm ->
            rm.insert(
                Dog(
                    name = "Star",
                    size = DogSize.L,
                    color = DogColor.DARK
                )
            )
        }
    }

    @Test
    fun enumSelect() {
        RacoonDen.getManager().use { rm ->
            val dog = rm.insert(
                Dog(
                    name = "Star",
                    size = DogSize.SMALL,
                    color = DogColor.LIGHT
                )
            )
            val dog2 = rm.find<Dog>(dog.id!!)
            assertEquals(DogSize.SMALL, dog2!!.size)
        }
    }

    @Test
    fun enumSelectWithColumnName() {
        RacoonDen.getManager().use { rm ->
            val dog = rm.insert(
                Dog(
                    name = "Star",
                    size = DogSize.L
                )
            )
            val dog2 = rm.find<Dog>(dog.id!!)
            assertEquals(DogSize.L, dog2!!.size)
        }
    }
}