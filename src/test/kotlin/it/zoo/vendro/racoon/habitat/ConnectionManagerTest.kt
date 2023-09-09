package it.zoo.vendro.racoon.habitat

import it.zoo.vendro.racoon.TestConfiguration
import it.zoo.vendro.racoon.definition.LazyId
import it.zoo.vendro.racoon.models.Cat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class ConnectionManagerTest {
    val verbose = false
    val pool = TestConfiguration.POOL

    @Test
    fun useCommit() {
        var catId: Int? = null
        pool.getManager().use {
            val cat = Cat(
                name = "test",
                age = 10,
                owner_id = null
            )
            it.insert(cat)
            catId = cat.id
        }
        pool.getManager().use {
            assertNotNull(catId)
            assertNotNull(it.find<Cat>(catId!!))
        }
    }

    @Test
    fun useRollback() {
        var catId: Int? = null
        runCatching {
            pool.getManager().use {
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
        pool.getManager().use {
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
        pool.getManager().use {
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
        pool.getManager().use { rm ->
            hash = rm.connection.hashCode()
            val cat = rm.find<Cat>(1)
            if(verbose) println(cat?.name)
        }
        Thread.sleep(4000)
        pool.getManager().use { rm ->
            val cat = rm.find<Cat>(1)
            if(verbose) println(cat?.name)
            assertNotEquals(hash, rm.connection.hashCode())
        }
    }

    @Test
    fun importSQLQuery() {
        pool.getManager().use { rm ->
            val res = rm.importQuery("test1.sql")
                .mapToString()
                .first()

            assertEquals("TEST VALUE", res)
        }
    }
}