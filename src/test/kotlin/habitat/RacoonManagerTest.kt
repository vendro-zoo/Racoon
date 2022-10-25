package habitat

import habitat.configuration.RacoonConfiguration
import habitat.definition.LazyId
import internals.configuration.ConnectionSettings
import internals.mappers.NameMapper
import models.Cat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class RacoonManagerTest {
    val verbose = false

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
    fun useCommit() {
        var catId: Int? = null
        RacoonDen.getManager().use {
            val cat = Cat(
                name = "test",
                age = 10,
                owner_id = null
            )
            it.insert(cat)
            catId = cat.id
        }
        RacoonDen.getManager().use {
            assertNotNull(catId)
            assertNotNull(it.find<Cat>(catId!!))
        }
    }

    @Test
    fun useRollback() {
        var catId: Int? = null
        runCatching {
            RacoonDen.getManager().use {
                val cat = Cat(
                    name = "test",
                    age = 10,
                    owner_id = LazyId.lazy(0, it)
                )
                it.insert(cat)
                catId = cat.id
            }
        }.fold(
            onSuccess = {
                fail("Should throw exception")
            },
            onFailure = {
                assertNull(catId)
            }
        )
    }

    @Test
    fun delete() {
        RacoonDen.getManager().use {
            val cat = Cat(
                name = "test",
                age = 10,
                owner_id = null
            )
            it.insert(cat)
            it.delete(cat)
            assertNull(it.find<Cat>(cat.id!!))
        }
    }

    @Test
    fun update() {
        RacoonDen.getManager().use {
            val cat = Cat(
                name = "test",
                age = 10,
                owner_id = null
            )
            it.insert(cat)
            cat.name = "test2"
            it.update(cat)
            assertEquals("test2", it.find<Cat>(cat.id!!)!!.name)
        }
    }

    @Test
    fun timeout() {
        var hash: Int? = null
        RacoonDen.getManager().use { rm ->
            hash = rm.connection.hashCode()
            val cat = rm.find<Cat>(1)
            if(verbose) println(cat?.name)
        }
        Thread.sleep(4000)
        RacoonDen.getManager().use { rm ->
            val cat = rm.find<Cat>(1)
            if(verbose) println(cat?.name)
            assertNotEquals(hash, rm.connection.hashCode())
        }
    }

    @Test
    fun importSQLQuery() {
        RacoonDen.getManager().use { rm ->
            val res = rm.importQueryRacoon("test1.sql")
                .mapToString()
                .first()

            assertEquals("TEST VALUE", res)
        }
    }
}