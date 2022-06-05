package habitat.configuration

import habitat.definition.LazyId
import internals.casting.ParameterCaster
import internals.casting.builtin.EnumCaster
import internals.casting.builtin.LazyCaster
import internals.configuration.ConnectionSettings
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
        private val parameterCasters: MutableMap<KClass<out Any>, ParameterCaster<out Any, out Any>> = mutableMapOf(
            LazyId::class to LazyCaster(),
            Enum::class to EnumCaster()
        )

        /**
         * Adds the given [ParameterCaster] to the list of registered list of [ParameterCaster]s.
         *
         * If a caster has already been registered for the given [KClass], it will be replaced.
         *
         * Example:
         * ```
         * // Adds a custom caster for [LazyId]
         * RacoonConfiguration.Casting.setCaster(LazyId::class, LazyCaster())
         * ```
         *
         * @param clazz The class of the parameter to cast.
         * @param caster The caster to use to cast the parameter.
         * @see [internals.casting.ParameterCaster]
         */
        fun <T: Any> setCaster(clazz: KClass<T>, caster: ParameterCaster<T, out Any>) = apply {
            parameterCasters[clazz] = caster
        }

        /**
         * Returns the [ParameterCaster] for the given [KClass].
         *
         * If no caster has been registered for the given [KClass],
         * it will try searching for a caster for the superclasses of the given [KClass].
         * If still no caster is found, it will return `null`.
         *
         * @param clazz The class of the parameter to cast.
         * @return The [ParameterCaster] for the given [KClass] or `null` if no caster is found.
         * @see [internals.casting.ParameterCaster]
         */
        fun getCaster(clazz: KClass<*>): ParameterCaster<Any, Any>? {
            @Suppress("UNCHECKED_CAST")
            return (parameterCasters[clazz] as ParameterCaster<Any, Any>?)
                ?: (clazz.superclasses.firstNotNullOfOrNull { parameterCasters[it] } as ParameterCaster<Any, Any>?)
        }
    }
}