package habitat.racoons

import habitat.RacoonDen
import habitat.RacoonManager
import habitat.configuration.RacoonConfiguration
import internals.configuration.ConnectionSettings
import internals.mappers.NameMapper
import models.Cat
import models.Owner
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class QueryRacoonTest {
    lateinit var racoonManager: RacoonManager

    // Configuring the connection settings
    companion object {
        @BeforeAll
        @JvmStatic
        internal fun setUpClass() {
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

        const val verbose = true
    }

    // Creating a new racoon manager
    @BeforeEach
    internal fun setUp() {
        racoonManager = RacoonDen.getManager()
    }

    // Closing the racoon manager
    @AfterEach
    internal fun tearDown() {
        try {
            racoonManager.release()
        } catch (_: Exception) {
            // Connection has already been released
        }
    }

    /**
     * Testing simple query mapping functionality
     */
    @Test
    internal fun singleQueryMapping() {
        val cats = racoonManager.createQueryRacoon("SELECT * FROM cat")
            .use { it.mapToClass<Cat>() }

        cats.forEach {
            assertNotNull(it)
            if (verbose) println(it)
        }
    }

    /**
     * Testing default alias functionality on simple query
     */
    @Test
    internal fun singleQueryDefaultAlias() {
        val cats = racoonManager.createQueryRacoon("SELECT c.* FROM cat c")
            .use { it.mapToClass<Cat>() }

        cats.forEach {
            assertNotNull(it)
            if (verbose) println(it)
        }
    }

    /**
     * Testing alias functionality on simple query
     */
    @Test
    internal fun singleQueryCustomAlias() {
        val cats = racoonManager.createQueryRacoon("SELECT alias.* FROM cat alias")
            .use {
                it.setAlias(Cat::class, "alias")
                    .mapToClass<Cat>()
            }

        cats.forEach {
            assertNotNull(it)
            if (verbose) println(it)
        }
    }

    /**
     * Testing mapping functionality on multiple query
     */
    @Test
    internal fun multiQueryMapping() {
        data class CatAndOwner(val cat: Cat, val owner: Owner?)

        val catAndOwners =
            racoonManager.createQueryRacoon("SELECT c.*, o.* FROM cat c left JOIN owner o ON c.owner_id = o.id")
                .use {
                    it.multiMapToClass<CatAndOwner>()
                }

        catAndOwners.forEach {
            assertNotNull(it)
            if (verbose) println(it)
        }
    }

    @Test
    internal fun lazyIdTest() {
        val cats = racoonManager.createQueryRacoon("SELECT alias.* FROM cat alias")
            .use {
                it.setAlias(Cat::class, "alias")
                    .mapToClass<Cat>()
            }.toMutableList()

        cats.forEach {
            val owner = it.owner_id.get()
            if (verbose) println(owner)
        }
    }

    @Test
    internal fun namedParameter() {
        val cats = racoonManager.createQueryRacoon("SELECT * FROM cat WHERE name = :name")
            .use {
                it.setParam("name", "Garfield")
                    .mapToClass<Cat>()
            }

        assertEquals(2, cats.size)
        assertEquals(1, cats[0].id)
    }

    @Test
    internal fun indexedParameter() {
        val cats = racoonManager.createQueryRacoon("SELECT * FROM cat WHERE name = ?")
            .use {
                it.setParam(1, "Garfield")
                    .mapToClass<Cat>()
            }

        assertEquals(2, cats.size)
        assertEquals(1, cats[0].id)
    }

    @Test
    internal fun countRows() {
        val count = racoonManager.createQueryRacoon("select * from cat")
            .use {
                it.execute().countRows()
            }
        if (verbose) println(count)
    }

    @Test
    internal fun mapToNumber() {
        val count: List<Int> = racoonManager.createQueryRacoon("select count(*) from cat")
            .use {
                it.execute().mapToNumber()
            }
        if (verbose) println(count)
    }

    @Test
    internal fun mapToString() {
        val name = racoonManager.createQueryRacoon("select name from cat")
            .use {
                it.execute().mapToString()
            }
        if (verbose) println(name)
    }
}