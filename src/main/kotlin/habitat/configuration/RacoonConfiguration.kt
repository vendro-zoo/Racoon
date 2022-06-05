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

    object Naming {
        var tableAliasMapper: (String) -> String = { it }
        var tableNameMapper: (String) -> String = { it }
        var columnNameMapper: (String) -> String = { it }
    }

    object Casting {
        private val parameterCasters: MutableMap<KClass<out Any>, ParameterCaster<out Any, out Any>> = mutableMapOf(
            LazyId::class to LazyCaster(),
            Enum::class to EnumCaster()
        )

        fun <T: Any> setCaster(clazz: KClass<T>, caster: ParameterCaster<T, out Any>) = apply {
            parameterCasters[clazz] = caster
        }

        fun getCaster(clazz: KClass<*>): ParameterCaster<Any, Any>? {
            @Suppress("UNCHECKED_CAST")
            return (parameterCasters[clazz] as ParameterCaster<Any, Any>?)
                ?: (clazz.superclasses.firstNotNullOfOrNull { parameterCasters[it] } as ParameterCaster<Any, Any>?)
        }
    }
}