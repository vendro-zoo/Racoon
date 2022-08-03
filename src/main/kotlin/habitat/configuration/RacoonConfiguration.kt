package habitat.configuration

import habitat.RacoonManager
import habitat.definition.LazyId
import internals.casting.ParameterCaster
import internals.casting.builtin.*
import internals.configuration.ConnectionSettings
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.superclasses

/**
 * An object containing the configuration for Racoon.
 */
@Suppress("unused")
object RacoonConfiguration {
    /**
     * All the configuration relative to the connection to the database.
     */
    object Connection {
        /**
         * The settings for the connection to the database.
         *
         * The default value is a [ConnectionSettings] with host "localhost" and database "test".
         */
        var connectionSettings: ConnectionSettings = ConnectionSettings(
            host = "localhost",
            database = "test"
        )
    }

    object Caching {
        /**
         * The maximum number of entries in the cache of each [RacoonManager].
         *
         * The default value is 100.
         */
        var maxEntries: Int = 100

        /**
         * The number of entries to remove from the cache when it is full.
         *
         * Note: the entries removed are the oldest ones.
         *
         * The default value is 20.
         */
        var cleaningBatchSize: Int = 20
    }

    /**
     * All the configuration for the default mapping of tables and columns names.
     */
    object Naming {
        /**
         * A lambda that will be used to map the alias of the table from the name of a given class.
         *
         * The default lambda is a function that returns the name of the class as-is.
         *
         * @see [internals.mappers.TableAliasMapper] for a list of built-in mappers.
         */
        var tableAliasMapper: (String) -> String = { it }

        /**
         * A lambda that will be used to map the name of the table from the name of a given class.
         *
         * The default lambda is a function that returns the name of the class as-is.
         *
         * @see [internals.mappers.NameMapper] for a list of built-in mappers.
         */
        var tableNameMapper: (String) -> String = { it }

        /**
         * A lambda that will be used to map the name of the column from the name of a given property.
         *
         * The default lambda is a function that returns the name of the property as-is.
         *
         * @see [internals.mappers.NameMapper] for a list of built-in mappers.
         */
        var columnNameMapper: (String) -> String = { it }
    }

    object Casting {
        val parameterCasters: MutableMap<KClass<out Any>, MutableMap<KClass<out Any>, ParameterCaster<out Any, out Any>>> =
            mutableMapOf(
                LazyId::class to mutableMapOf(
                    Int::class to LazyCaster(),
                    Long::class to LazyLongCaster()
                ),
                Enum::class to mutableMapOf(String::class to EnumCaster()),
                Date::class to mutableMapOf(
                    java.sql.Date::class to DateCaster(),
                    java.sql.Timestamp::class to DateTimestampCaster()
                )
            )

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
         * @see [internals.casting.ParameterCaster]
         */
        fun <J : Any, S : Any> setCaster(jClass: KClass<J>, sClass: KClass<S>, caster: ParameterCaster<J, S>) = apply {
            val m1 = parameterCasters[jClass]
                ?: mutableMapOf<KClass<out Any>, ParameterCaster<out Any, out Any>>().apply {
                    parameterCasters[jClass] = this
                }
            m1.putIfAbsent(sClass, caster)
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
         * @see [internals.casting.ParameterCaster]
         */
        fun getCaster(jClass: KClass<*>, sClass: KClass<*>): ParameterCaster<Any, Any>? {
            val m1 = parameterCasters[jClass]
                ?: jClass.superclasses.firstNotNullOfOrNull { parameterCasters[it] }
                ?: return null

            val m2 = m1[sClass]
                ?: sClass.superclasses.firstNotNullOfOrNull { m1[it] }
                ?: return null

            @Suppress("UNCHECKED_CAST")
            return m2 as ParameterCaster<Any, Any>
        }

        fun getFirstCaster(jClass: KClass<*>): ParameterCaster<Any, Any>? {
            val m1 = parameterCasters[jClass]
                ?: jClass.superclasses.firstNotNullOfOrNull { parameterCasters[it] }
                ?: return null

            @Suppress("UNCHECKED_CAST")
            return m1.values.first() as ParameterCaster<Any, Any>
        }
    }
}