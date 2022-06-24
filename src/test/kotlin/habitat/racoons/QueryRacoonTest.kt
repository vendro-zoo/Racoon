package habitat.racoons

import habitat.RacoonDen
import habitat.RacoonManager
import habitat.configuration.RacoonConfiguration
import habitat.definition.ColumnName
import habitat.definition.LazyId
import habitat.definition.Table
import habitat.definition.TableName
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
            RacoonConfiguration.Connection.connectionSettings =
                ConnectionSettings(
                    host = "localhost",
                    port = 3306,
                    database = "racoon-ktor-sample",
                    username = "admin",
                    password = "admin"
                )
            RacoonConfiguration.Naming.tableNameMapper = NameMapper.lowerSnakeCase
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
            racoonManager.createQueryRacoon("SELECT c.*, o.* FROM cat c LEFT JOIN owner o ON c.owner_id = o.id")
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
            val owner = it.owner_id?.get()
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
        val count = racoonManager.createQueryRacoon("SELECT * FROM cat")
            .use {
                it.execute().countRows()
            }
        if (verbose) println(count)
    }

    @Test
    internal fun mapToNumber() {
        val count: List<Int?> = racoonManager.createQueryRacoon("SELECT COUNT(*) FROM cat")
            .use {
                it.execute().mapToNumber()
            }
        if (verbose) println(count)
    }

    @Test
    internal fun mapToString() {
        val name = racoonManager.createQueryRacoon("SELECT name FROM cat")
            .use {
                it.execute().mapToString()
            }
        if (verbose) println(name)
    }

    @Test
    internal fun mapToCustom() {
        RacoonDen.getManager().use { rm ->
            val pair = rm.createQueryRacoon("SELECT 5, 3")
                .mapToCustom { it.getInt(1) to it.getInt(2) }.first()

            assertEquals(5, pair.first)
            assertEquals(3, pair.second)
        }
    }

    @Test
    internal fun customName() {
        @TableName("cat")
        data class CustomCat(
            override var id: Int? = null,
            @property:ColumnName("age")
            @param:ColumnName("age")
            var AGE: Int,
            @property:ColumnName("name")
            @param:ColumnName("name")
            var NAME: String?,
            @property:ColumnName("owner_id")
            @param:ColumnName("owner_id")
            var OWNERID: LazyId<Owner>? = null,
        ) : Table

        RacoonDen.getManager().use { rm ->
            val cat = rm.createQueryRacoon("SELECT * FROM cat")
                .mapToClass<CustomCat>().first()

            assertNotNull(cat.id)
        }
    }

    @Test
    internal fun inQuery() {
        val cats = RacoonDen.getManager().use { rm ->
            rm.createQueryRacoon("SELECT * FROM cat WHERE name IN (:names)")
                .setParam("names", listOf("Tom",
                        "Garfield",
                        "Jim"
                ))
                .mapToClass<Cat>()
        }

        cats.forEach {
            assert(listOf("Tom",
                "Garfield",
                "Jim"
            ).contains(it.name))
        }
    }
}