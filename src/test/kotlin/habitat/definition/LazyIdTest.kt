package habitat.definition

import habitat.RacoonDen
import habitat.configuration.RacoonConfiguration
import internals.configuration.ConnectionSettings
import internals.mappers.NameMapper
import models.Cat
import models.Owner
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

internal class LazyIdTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun setUp() {
            RacoonConfiguration.Connection.setDefault(
                ConnectionSettings(
                    host = "localhost",
                    port = 3306,
                    database = "racoon-ktor-sample",
                    username = "admin",
                    password = "admin",
                    idleTimeout = 3
                )
            )
            RacoonConfiguration.Naming.setTableNameMapper(NameMapper.lowerSnakeCase)        }
    }

    @Test
    fun insertLazy() {
        RacoonDen.getManager().use { rm ->
            val cat = rm.insert(
                Cat(
                    name = "Kitty",
                    age = 3,
                    owner_id = LazyId.lazy(2, rm)
                )
            )

            assertNotNull(cat.owner_id.id)
            assertNotNull(cat.id)
        }
    }

    @Test
    fun insertDefined() {
        RacoonDen.getManager().use { rm ->
            val owner = rm.find<Owner>(2)!!
            val cat = rm.insert(
                Cat(
                    name = "Kitty",
                    age = 3,
                    owner_id = LazyId.defined(owner)
                )
            )

            assertNotNull(cat.owner_id.id)
            assertNotNull(cat.id)
        }
    }

    @Test
    fun insertEmpty() {
        RacoonDen.getManager().use { rm ->
            val cat = rm.insert(
                Cat(
                    name = "Kitty",
                    age = 3,
                )
            )

            assertNull(cat.owner_id.id)
            assertNotNull(cat.id)
        }
    }

    @Test
    fun evaluateLazy() {
        RacoonDen.getManager().use { rm ->
            val cat = rm.find<Cat>(1)!!

            assertNotNull(cat.owner_id.id)
            assertNotNull(cat.owner_id.get())
            assertNotNull(cat.id)
        }
    }
}