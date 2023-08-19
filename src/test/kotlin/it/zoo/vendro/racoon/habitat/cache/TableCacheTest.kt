package it.zoo.vendro.racoon.habitat.cache

import it.zoo.vendro.racoon.habitat.ConnectionPool
import it.zoo.vendro.racoon.habitat.configuration.RacoonConfiguration
import it.zoo.vendro.racoon.internals.configuration.ConnectionSettings
import it.zoo.vendro.racoon.internals.mappers.NameMapper
import it.zoo.vendro.racoon.internals.protocols.PostgresSQLProtocol
import it.zoo.vendro.racoon.models.Cat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class TableCacheTest {
    val pool = ConnectionPool(
        RacoonConfiguration(
            connection = RacoonConfiguration.Connection(
                ConnectionSettings(
                    host = "localhost",
                    port = 5432,
                    database = "racoon",
                    username = "admin",
                    password = "admin",
                    protocol = PostgresSQLProtocol(),
                    idleTimeout = 0
                )
            ),
            naming = RacoonConfiguration.Naming(
                tableNameMapper = NameMapper.lowerSnakeCase,
                tableAliasMapper = NameMapper.lowerSnakeCase
            ),
            caching = RacoonConfiguration.Caching(
                maxEntries = 1,
            )
        )
    )

    @Test
    fun repeatedFind() {
        pool.getManager().use { rm ->
            val cat = rm.createQuery("SELECT * FROM cat LIMIT 1")
                .mapToClass<Cat>()
                .first().id!!

            val start = System.currentTimeMillis()
            for (i in 0..2000) {
                rm.find<Cat>(cat)
            }
            val end = System.currentTimeMillis()
            assertTrue(end - start < 50, "The cache took too long to be used")
        }
    }

    @Test
    fun replaceInCacheFind() {
        pool.getManager().use { rm ->
            val cat1 = rm.createQuery("SELECT * FROM cat LIMIT 1")
                .mapToClass<Cat>()
                .first().id!!
            val cat2 = rm.createQuery("SELECT * FROM cat LIMIT 1 OFFSET 1")
                .mapToClass<Cat>()
                .first().id!!

            rm.find<Cat>(cat1)

            assertEquals(1, rm.cache.cacheSize)
            assertTrue(rm.cache.cache.containsKey(Cat::class))
            assertTrue(rm.cache.cache[Cat::class]!!.containsKey(cat1))

            rm.find<Cat>(cat2)

            assertEquals(1, rm.cache.cacheSize)
            assertTrue(rm.cache.cache.containsKey(Cat::class))
            assertTrue(rm.cache.cache[Cat::class]!!.containsKey(cat2))
        }
    }

    @Test
    fun insertCache() {
        pool.getManager().use { rm ->
            val cat = Cat(name = "test", age = 10)
            rm.insert(cat)

            assertEquals(1, rm.cache.cacheSize)
            assertTrue(rm.cache.cache.containsKey(Cat::class))
            assertTrue(rm.cache.cache[Cat::class]!!.containsKey(cat.id!!))
        }
    }
}