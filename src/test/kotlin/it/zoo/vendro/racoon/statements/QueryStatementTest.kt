package it.zoo.vendro.racoon.statements

import it.zoo.vendro.racoon.TestConfiguration
import it.zoo.vendro.racoon.connection.ConnectionManager
import it.zoo.vendro.racoon.definition.*
import models.Cat
import models.Cats
import models.Owner
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class QueryStatementTest {
    val pool = TestConfiguration.POOL

    lateinit var connectionManager: ConnectionManager

    // Configuring the connection settings
    companion object {
        const val verbose = true
    }

    // Creating a new racoon manager
    @BeforeEach
    internal fun setUp() {
        connectionManager = pool.getManager()
    }

    // Closing the racoon manager
    @AfterEach
    internal fun tearDown() {
        try {
            connectionManager.release()
        } catch (_: Exception) {
            // Connection has already been released
        }
    }

    /**
     * Testing simple query mapping functionality
     */
    @Test
    internal fun singleQueryMapping() {
        val cats = connectionManager.createQuery("SELECT * FROM cat")
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
        val cats = connectionManager.createQuery("SELECT c.* FROM cat c")
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
        val cats = connectionManager.createQuery("SELECT alias.* FROM cat alias")
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
            connectionManager.createQuery("SELECT c.*, o.* FROM cat c LEFT JOIN owner o ON c.owner_id = o.id")
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
        val cats = connectionManager.createQuery("SELECT alias.* FROM cat alias")
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
        val cats = connectionManager.createQuery("SELECT * FROM cat WHERE name = :name")
            .use {
                it.setParam("name", "Garfield")
                    .mapToClass<Cat>()
            }

        assertEquals(2, cats.size)
        assertEquals(1, cats[0].id)
    }

    @Test
    internal fun indexedParameter() {
        val cats = connectionManager.createQuery("SELECT * FROM cat WHERE name = ?")
            .use {
                it.setParam(1, "Garfield")
                    .mapToClass<Cat>()
            }

        assertEquals(2, cats.size)
        assertEquals(1, cats[0].id)
    }

    @Test
    internal fun mapToNumber() {
        val count: List<Int> = connectionManager.createQuery("SELECT COUNT(*) FROM cat")
            .use {
                it.execute().mapToInt()
            }
        if (verbose) println(count)
    }

    @Test
    internal fun mapToNullNumber() {
        val count: List<Int?> = connectionManager.createQuery("SELECT NULL")
            .use {
                it.execute().mapToNullableInt()
            }
        if (verbose) println(count)
    }

    @Test
    internal fun mapToString() {
        val name = connectionManager.createQuery("SELECT name FROM cat")
            .use {
                it.execute().mapToString()
            }
        if (verbose) println(name)
    }

    @Test
    internal fun mapToCustom() {
        pool.getManager().use { rm ->
            val pair = rm.createQuery("SELECT 5, 3")
                .consumeRows { it.resultSet.getInt(1) to it.resultSet.getInt(2) }.first()

            assertEquals(5, pair.first)
            assertEquals(3, pair.second)
        }
    }

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
        var OWNERID: LazyId<Int, Owner>? = null,
    ) : Table<Int, CustomCat> {
        @ColumnIgnore
        override val tableInfo = CustomCats
    }

    object CustomCats : TableInfo<Int, CustomCat> {
        override val tbKClass = CustomCat::class
        override val idType = typeOf<Int>()
    }

    @Test
    internal fun customName() {
        pool.getManager().use { rm ->
            val cat = rm.createQuery("SELECT * FROM cat")
                .mapToClass<CustomCat>().first()

            assertNotNull(cat.id)
        }
    }

    @Test
    internal fun inQuery() {
        val cats = pool.getManager().use { rm ->
            rm.createQuery("SELECT * FROM cat WHERE name IN (:names)")
                .setParam(
                    "names", listOf(
                        "Tom",
                        "Garfield",
                        "Jim"
                    )
                )
                .mapToClass<Cat>()
        }

        cats.forEach {
            assert(
                listOf(
                    "Tom",
                    "Garfield",
                    "Jim"
                ).contains(it.name)
            )
        }
    }

    @Test
    internal fun inQuery2() {
        val cats = pool.getManager().use { rm ->
            rm.createQuery("SELECT * FROM cat WHERE name IN (?)")
                .setParam(
                    1, listOf(
                        "Tom",
                        "Garfield",
                        "Jim"
                    )
                )
                .mapToClass<Cat>()
        }

        cats.forEach {
            assert(
                listOf(
                    "Tom",
                    "Garfield",
                    "Jim"
                ).contains(it.name)
            )
        }
    }
}