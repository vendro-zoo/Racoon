package habitat

import commons.configuration.ConnectionSettings
import commons.mappers.NameMapper
import habitat.configuration.RacoonConfiguration
import habitat.definition.LazyId
import models.Cat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class RacoonManagerTest {

    @BeforeEach
    fun setUp() {
        RacoonConfiguration.Connection.setDefault(
            ConnectionSettings(
                host = "localhost",
                port = 3306,
                database = "racoon-ktor-sample",
                username = "admin",
                password = "admin"
            )
        )
        RacoonConfiguration.Naming.setTableNameMapper(NameMapper.lowerSnakeCase)
    }

    @Test
    fun useCommit() {
        var catId: Int? = null
        RacoonDen.getManager().use {
            val cat = Cat(
                name = "test",
                age = 10,
                owner_id = LazyId.empty()
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
                owner_id = LazyId.empty()
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
                owner_id = LazyId.empty()
            )
            it.insert(cat)
            cat.name = "test2"
            it.update(cat)
            assertEquals("test2", it.find<Cat>(cat.id!!)!!.name)
        }
    }
}