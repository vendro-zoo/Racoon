package habitat.configuration

import habitat.definition.LazyId
import internals.casting.ParameterCaster
import internals.casting.builtin.EnumCaster
import internals.casting.builtin.LazyCaster
import internals.configuration.ConnectionSettings
import internals.mappers.NameMapper
import internals.mappers.TableAliasMapper
import kotlin.reflect.KClass
import kotlin.reflect.full.superclasses

@Suppress("unused")
object RacoonConfiguration {
    object Connection {
        private var defaultConnectionSettings: ConnectionSettings = ConnectionSettings(
            host = "localhost",
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
        private var defaultTableAliasMapper: (String) -> String = TableAliasMapper.onlyUpperToLower
        private var defaultTableNameMapper: (String) -> String = NameMapper.lowerSnakeCase
        private var defaultColumnNameMapper: (String) -> String = NameMapper.lowerSnakeCase

        fun setTableAliasMapper(mapper: (String) -> String) {
            defaultTableAliasMapper = mapper
        }

        fun getTableAlias(tableName: String): String {
            return defaultTableAliasMapper(tableName)
        }

        fun setTableNameMapper(mapper: (String) -> String) {
            defaultTableNameMapper = mapper
        }

        fun getTableName(tableAlias: String): String {
            return defaultTableNameMapper(tableAlias)
        }

        fun setColumnNameMapper(mapper: (String) -> String) {
            defaultColumnNameMapper = mapper
        }

        fun getColumnName(columnName: String): String {
            return defaultColumnNameMapper(columnName)
        }
    }

    object Casting {
        private val parameterCasters: MutableMap<KClass<out Any>, ParameterCaster<out Any, out Any>> = mutableMapOf(
            LazyId::class to LazyCaster(),
            Enum::class to EnumCaster()
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