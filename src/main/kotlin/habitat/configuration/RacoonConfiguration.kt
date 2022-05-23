package habitat.configuration

import commons.casting.ParameterCaster
import commons.casting.builtin.LazyCaster
import commons.configuration.ConnectionSettings
import commons.mappers.NameMapper
import commons.mappers.TableAliasMappers
import habitat.definition.LazyId
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

    object Naming {
        private var defaultTableAliasMapper: (String) -> String = TableAliasMappers.onlyUpperToLower
        private var defaultNameMapper: (String) -> String = NameMapper.lowerSnakeCase

        fun setTableAliasMapper(mapper: (String) -> String) {
            defaultTableAliasMapper = mapper
        }

        fun getTableAlias(tableName: String): String {
            return defaultTableAliasMapper(tableName)
        }

        fun setNameMapper(mapper: (String) -> String) {
            defaultNameMapper = mapper
        }

        fun getName(tableAlias: String): String {
            return defaultNameMapper(tableAlias)
        }
    }

    object Casting {
        private val parameterCasters: MutableMap<KClass<out Any>, ParameterCaster<out Any, out Any>> = mutableMapOf(
            LazyId::class to LazyCaster()
        )

        fun <T: Any> setCaster(clazz: KClass<T>, caster: ParameterCaster<T, out Any>) {
            parameterCasters[clazz] = caster
        }

        fun getCaster(clazz: KClass<*>): ParameterCaster<Any, Any>? {
            @Suppress("UNCHECKED_CAST")
            return (parameterCasters[clazz] as ParameterCaster<Any, Any>?)
                ?: (clazz.superclasses.firstNotNullOfOrNull { parameterCasters[it] } as ParameterCaster<Any, Any>?)
        }
    }
}