package it.zoo.vendro.racoon.habitat.configuration

import it.zoo.vendro.racoon.habitat.ConnectionManager
import it.zoo.vendro.racoon.habitat.definition.LazyId
import it.zoo.vendro.racoon.internals.casting.ParameterCaster
import it.zoo.vendro.racoon.internals.casting.builtin.*
import it.zoo.vendro.racoon.internals.configuration.ConnectionSettings
import java.math.BigInteger
import java.sql.Timestamp
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.superclasses

/**
 * An object containing the configuration for Racoon.
 */
@Suppress("unused")
class RacoonConfiguration(
    val connection: Connection = Connection(),
    val caching: Caching = Caching(),
    val naming: Naming = Naming(),
    val casting: Casting = Casting(),
    val resourcing: Resourcing = Resourcing()
) {
    /**
     * All the configuration relative to the connection to the database.
     */
    class Connection(
        /**
         * The settings for the connection to the database.
         *
         * The default value is a [ConnectionSettings] with host "localhost" and database "test".
         */
        val connectionSettings: ConnectionSettings = ConnectionSettings(
            host = "localhost",
            database = "test"
        )
    )

    class Caching(
        /**
         * The maximum number of entries in the cache of each [ConnectionManager].
         *
         * The default value is 100.
         */
        val maxEntries: Int = 100,
        /**
         * The number of entries to remove from the cache when it is full.
         *
         * Note: the entries removed are the oldest ones.
         *
         * The default value is 20.
         */
        val cleaningBatchSize: Int = 20
    )

    /**
     * All the configuration for the default mapping of tables and columns names.
     */
    class Naming(
        /**
         * A lambda that will be used to map the alias of the table from the name of a given class.
         *
         * The default lambda is a function that returns the name of the class as-is.
         *
         * @see [it.zoo.vendro.racoon.internals.mappers.TableAliasMapper] for a list of built-in mappers.
         */
        val tableAliasMapper: (String) -> String = { it },

        /**
         * A lambda that will be used to map the name of the table from the name of a given class.
         *
         * The default lambda is a function that returns the name of the class as-is.
         *
         * @see [it.zoo.vendro.racoon.internals.mappers.NameMapper] for a list of built-in mappers.
         */
        val tableNameMapper: (String) -> String = { it },

        /**
         * A lambda that will be used to map the name of the column from the name of a given property.
         *
         * The default lambda is a function that returns the name of the property as-is.
         *
         * @see [it.zoo.vendro.racoon.internals.mappers.NameMapper] for a list of built-in mappers.
         */
        val columnNameMapper: (String) -> String = { it }
    )

    class Casting(
        val parameterCasters: MutableMap<WKClass<out Any>, MutableMap<WKClass<out Any>, ParameterCaster<out Any, out Any>>> =
            mutableMapOf(
                WKClass(LazyId::class) to mutableMapOf(
                    WKClass(Int::class) to LazyCaster(),
                    WKClass(Long::class) to LazyLongCaster(),
                    WKClass(BigInteger::class) to LazyBigIntegerCaster()
                ),
                WKClass(Enum::class) to mutableMapOf(WKClass(String::class) to EnumCaster()),
                WKClass(Date::class) to mutableMapOf(
                    WKClass(java.sql.Date::class) to DateCaster(),
                    WKClass(Timestamp::class) to DateTimestampCaster()
                )
            )
    ) {
        data class WKClass<T : Any>(val klass: KClass<T>) {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as WKClass<*>

                if (klass != other.klass) return false

                return true
            }

            override fun hashCode(): Int {
                return klass.qualifiedName?.hashCode() ?: 0
            }
        }

        /**
         * Adds the given [ParameterCaster] to the list of registered list of [ParameterCaster]s.
         *
         * If a caster has already been registered for the given [KClass], it will be replaced.
         *
         * Example:
         * ```
         * // Adds a custom caster for [LazyId]
         * RacoonConfiguration.Casting.setCaster(LazyId::class, Int::class, LazyCaster())
         * ```
         *
         * @param jClass The class of the parameter to cast (Java type).
         * @param sClass The class of the parameter to cast to (SQL type).
         * @param caster The caster to use to cast the parameter.
         * @see [it.zoo.vendro.racoon.internals.casting.ParameterCaster]
         */
        fun <J : Any, S : Any> setCaster(jClass: KClass<J>, sClass: KClass<S>, caster: ParameterCaster<J, S>) = apply {
            val wJClass = WKClass(jClass)
            val wSClass = WKClass(sClass)

            val m1 = parameterCasters[wJClass]
                ?: mutableMapOf<WKClass<out Any>, ParameterCaster<out Any, out Any>>().apply {
                    parameterCasters[wJClass] = this
                }
            m1.putIfAbsent(wSClass, caster)
        }

        /**
         * Returns the [ParameterCaster] for the given [KClass].
         *
         * If no caster has been registered for the given [KClass],
         * it will try searching for a caster for the superclasses of the given [KClass].
         * If still no caster is found, it will return `null`.
         *
         * @param jClass The class of the parameter to cast (Java type).
         * @param sClass The class of the parameter to cast to (SQL type).
         * @return The [ParameterCaster] for the given [KClass] or `null` if no caster is found.
         * @see [it.zoo.vendro.racoon.internals.casting.ParameterCaster]
         */
        fun getCaster(jClass: KClass<*>, sClass: KClass<*>): ParameterCaster<Any, Any>? {
            val wJClass = WKClass(jClass)
            val wSClass = WKClass(sClass)

            val m1 = parameterCasters[wJClass]
                ?: jClass.superclasses.firstNotNullOfOrNull { parameterCasters[WKClass(it)] }
                ?: return null

            val m2 = m1[wSClass]
                ?: sClass.superclasses.firstNotNullOfOrNull { m1[WKClass(it)] }
                ?: return null

            @Suppress("UNCHECKED_CAST")
            return m2 as ParameterCaster<Any, Any>
        }

        fun getFirstCaster(jClass: KClass<*>): ParameterCaster<Any, Any>? {
            val wJClass = WKClass(jClass)

            val m1 = parameterCasters[wJClass]
                ?: jClass.superclasses.firstNotNullOfOrNull { parameterCasters[WKClass(it)] }
                ?: return null

            @Suppress("UNCHECKED_CAST")
            return m1.values.first() as ParameterCaster<Any, Any>
        }
    }

    /**
     * All the configuration for resource handling.
     */
    class Resourcing(
        /**
         * The base path for sql files.
         */
        val baseSQLPath: String = "sql"
    )
}