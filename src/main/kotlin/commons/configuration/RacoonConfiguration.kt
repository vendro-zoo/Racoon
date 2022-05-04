package commons.configuration

import commons.TableAliasMappers
import commons.declarations.ParameterCaster
import commons.declarations.casters.BooleanCaster
import commons.declarations.casters.StringCaster
import kotlin.reflect.KClass

object RacoonConfiguration {
    var defaultConnectionSettings: ConnectionSettings? = null
    var defaultTableAliasMapper: (String) -> String = TableAliasMappers.onlyUpperToLower
    val parameterCasters: MutableMap<KClass<*>, ParameterCaster<*>> = mutableMapOf(
        Pair(String::class, StringCaster()),
        Pair(Boolean::class, BooleanCaster()),
    )
}