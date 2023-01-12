package it.zoo.vendro.internals.casting.builtin

import it.zoo.vendro.racoon.habitat.ConnectionPool
import it.zoo.vendro.racoon.habitat.configuration.RacoonConfiguration
import it.zoo.vendro.racoon.internals.configuration.ConnectionSettings
import it.zoo.vendro.racoon.internals.mappers.NameMapper
import it.zoo.vendro.racoon.models.Dog
import it.zoo.vendro.racoon.models.DogColor
import it.zoo.vendro.racoon.models.DogSize
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class EnumCasterTest {
    val pool = ConnectionPool(
        RacoonConfiguration(
            connection = RacoonConfiguration.Connection(
                ConnectionSettings(
                    host = "localhost",
                    port = 3306,
                    database = "racoon-ktor-sample",
                    username = "admin",
                    password = "admin",
                    idleTimeout = 3
                )
            ),
            naming = RacoonConfiguration.Naming(
                tableNameMapper = NameMapper.lowerSnakeCase,
            )
        )
    )

    @Test
    fun enumInsert() {
        pool.getManager().use { rm ->
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
        pool.getManager().use { rm ->
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
        pool.getManager().use { rm ->
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
        pool.getManager().use { rm ->
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