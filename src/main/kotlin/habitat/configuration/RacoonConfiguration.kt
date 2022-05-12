package habitat.configuration

import commons.mappers.TableAliasMappers
import commons.casting.ParameterCaster
import commons.configuration.ConnectionSettings
import kotlin.reflect.KClass
import kotlin.reflect.full.superclasses

@Suppress("unused")
object RacoonConfiguration {
    object Connection {
        private var defaultConnectionSettings: ConnectionSettings = ConnectionSettings(
            host = "test",
            database = "test"
        )

        fun setDefault(settings: ConnectionSettings) {
            defaultConnectionSettings = settings
        }

        fun getDefault(): ConnectionSettings {
            return defaultConnectionSettings
        }
    }

    object TableAliases {
        private var defaultTableAliasMapper: (String) -> String = TableAliasMappers.onlyUpperToLower

        fun setAliasMapper(mapper: (String) -> String) {
            defaultTableAliasMapper = mapper
        }

        fun getAlias(tableName: String): String {
            return defaultTableAliasMapper(tableName)
        }
    }

    object Casting {
        private val parameterCasters: MutableMap<KClass<out Any>, ParameterCaster<out Any, out Any>> = mutableMapOf()

        fun <T: Any> setCaster(clazz: KClass<T>, caster: ParameterCaster<T, out Any>) {
            parameterCasters[clazz] = caster
        }

        fun getCaster(clazz: KClass<*>): ParameterCaster<out Any, out Any>? {
            return parameterCasters[clazz]
                ?: clazz.superclasses.firstNotNullOfOrNull { parameterCasters[it] }
        }
    }
}