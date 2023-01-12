package it.zoo.vendro.habitat.cache

import it.zoo.vendro.racoon.habitat.RacoonDen
import it.zoo.vendro.racoon.habitat.configuration.RacoonConfiguration
import it.zoo.vendro.racoon.internals.configuration.ConnectionSettings
import it.zoo.vendro.racoon.internals.mappers.NameMapper
import it.zoo.vendro.racoon.models.Cat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class RacoonCacheTest {

    @BeforeEach
    fun setUp() {
        RacoonConfiguration.Connection.connectionSettings =
            ConnectionSettings(
                host = "localhost",
                port = 3306,
                database = "racoon-ktor-sample",
                username = "admin",
                password = "admin",
                idleTimeout = 300
            )
        RacoonConfiguration.Naming.tableNameMapper = NameMapper.lowerSnakeCase
        RacoonConfiguration.Naming.tableAliasMapper = NameMapper.lowerSnakeCase
        RacoonConfiguration.Caching.maxEntries = 1
    }

    @Test
    fun repeatedFind() {
        RacoonDen.getManager().use { rm ->
            val cat = rm.createQueryRacoon("SELECT * FROM cat LIMIT 1")
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
        RacoonDen.getManager().use { rm ->
            val cat1 = rm.createQueryRacoon("SELECT * FROM cat LIMIT 1")
                .mapToClass<Cat>()
                .first().id!!
            val cat2 = rm.createQueryRacoon("SELECT * FROM cat LIMIT 1 OFFSET 1")
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
        RacoonDen.getManager().use { rm ->
            val cat = Cat(name = "test", age = 10)
            rm.insert(cat)

            assertEquals(1, rm.cache.cacheSize)
            assertTrue(rm.cache.cache.containsKey(Cat::class))
            assertTrue(rm.cache.cache[Cat::class]!!.containsKey(cat.id!!))
        }
    }
}