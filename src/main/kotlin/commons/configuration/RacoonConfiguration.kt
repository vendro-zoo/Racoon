package commons.configuration

import commons.mappers.TableAliasMappers
import commons.casting.ParameterCaster
import commons.casting.casters.*
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.superclasses

@Suppress("unused")
object RacoonConfiguration {
    object Connection {
        private var defaultConnectionSettings: ConnectionSettings? = null
        fun setDefault(settings: ConnectionSettings) {
            defaultConnectionSettings = settings
        }

        fun getDefault(): ConnectionSettings? {
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
        private val parameterCasters: MutableMap<KClass<out Any>, ParameterCaster<out Any>> = mutableMapOf(
            Pair(String::class, StringCaster()),
            Pair(Boolean::class, BooleanCaster()),
            Pair(Char::class, CharCaster()),
            Pair(Date::class, DateCaster()),
            Pair(Number::class, NumberCaster())
        )

        fun <T: Any> setCaster(clazz: KClass<T>, caster: ParameterCaster<T>) {
            parameterCasters[clazz] = caster
        }

        fun getCaster(clazz: KClass<*>): ParameterCaster<out Any> {
            return parameterCasters[clazz]
                ?: clazz.superclasses.firstNotNullOfOrNull { parameterCasters[it] }
                ?: throw NoSuchMethodException("A ParameterCaster for the class '${clazz.simpleName}' has not been registered")
        }
    }
}