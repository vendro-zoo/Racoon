package it.zoo.vendro.racoon.habitat.definition

import it.zoo.vendro.racoon.TestConfiguration
import it.zoo.vendro.racoon.models.Cat
import it.zoo.vendro.racoon.models.Owner
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class LazyIdTest {
    val pool = TestConfiguration.POOL

    @Test
    fun insertLazy() {
        pool.getManager().use { rm ->
            val cat = rm.insert(
                Cat(
                    name = "Kitty",
                    age = 3,
                    owner_id = LazyId.lazy(2, rm)
                )
            )

            assertNotNull(cat.owner_id?.id)
            assertNotNull(cat.id)
        }
    }

    @Test
    fun insertDefined() {
        pool.getManager().use { rm ->
            val owner = rm.find<Owner>(2)!!
            val cat = rm.insert(
                Cat(
                    name = "Kitty",
                    age = 3,
                    owner_id = LazyId.defined(owner)
                )
            )

            assertNotNull(cat.owner_id?.id)
            assertNotNull(cat.id)
        }
    }

    @Test
    fun insertEmpty() {
        pool.getManager().use { rm ->
            val cat = rm.insert(
                Cat(
                    name = "Kitty",
                    age = 3,
                )
            )

            assertNull(cat.owner_id?.id)
            assertNotNull(cat.id)
        }
    }

    @Test
    fun evaluateLazy() {
        pool.getManager().use { rm ->
            val cat = rm.find<Cat>(1)!!

            assertNotNull(cat.owner_id?.id)
            assertNotNull(cat.owner_id?.get())
            assertNotNull(cat.id)
        }
    }
}