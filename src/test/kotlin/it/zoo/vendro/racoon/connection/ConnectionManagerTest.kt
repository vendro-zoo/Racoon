package it.zoo.vendro.racoon.connection

import it.zoo.vendro.racoon.TestConfiguration
import models.Cat
import models.Cats
import models.Owners
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
            assertNotNull(Cats.find(it, catId!!))
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
                    owner_id = Owners.lazy(0, it)
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
            assertNull(Cats.find(it, cat.id!!))
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
            assertEquals("test2", Cats.find(it, cat.id!!)!!.name)
        }
    }

    @Test
    fun timeout() {
        var hash: Int? = null
        pool.getManager().use { rm ->
            hash = rm.connection.hashCode()
            val cat = Cats.find(rm, 1)
            if (verbose) println(cat?.name)
        }
        Thread.sleep(4000)
        pool.getManager().use { rm ->
            val cat = Cats.find(rm, 1)
            if (verbose) println(cat?.name)
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