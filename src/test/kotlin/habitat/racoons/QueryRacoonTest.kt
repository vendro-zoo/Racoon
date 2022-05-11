package habitat.racoons

import commons.configuration.ConnectionSettings
import habitat.RacoonManager
import habitat.configuration.RacoonConfiguration
import models.Cat
import models.Owner
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
                    database = "test",
                    username = "test",
                    password = "test"
                )
            )
        }

        const val verbose = true
    }

    // Creating a new racoon manager
    @BeforeEach
    internal fun setUp() {
        racoonManager = RacoonManager.create()
    }

    // Closing the racoon manager
    @AfterEach
    internal fun tearDown() {
        racoonManager.close()
    }

    /**
     * Testing simple query mapping functionality
     */
    @Test
    internal fun singleQueryMapping() {
        val cats = racoonManager.createQueryRacoon("SELECT * FROM cat")
            .use { it.mapToClass<Cat>() }

        assert(cats.size == 3)
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

        assert(cats.size == 3)
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

        assert(cats.size == 3)
        cats.forEach {
            assertNotNull(it)
            if (verbose) println(it)
        }
    }

    /**
     * Testing alias functionality on simple query
     */
    @Test
    internal fun multiQueryMapping() {
        data class CatAndOwner(val cat: Cat, val owner: Owner)

        val catAndOwners = racoonManager.createQueryRacoon("SELECT c.*, o.* FROM cat c JOIN owner o ON c.owner_id = o.id")
            .use {
                it.multiMapToClass<CatAndOwner>()
            }

        assert(catAndOwners.size == 1)
        catAndOwners.forEach {
            assertNotNull(it)
            if (verbose) println(it)
        }
    }
}