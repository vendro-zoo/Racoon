package commons.configuration

import commons.TableAliasMappers
import commons.declarations.ParameterCaster
import commons.declarations.casters.BooleanCaster
import commons.declarations.casters.CharCaster
import commons.declarations.casters.DateCaster
import commons.declarations.casters.StringCaster
import java.util.*
import kotlin.reflect.KClass

object RacoonConfiguration {
    var defaultConnectionSettings: ConnectionSettings? = null
    var defaultTableAliasMapper: (String) -> String = TableAliasMappers.onlyUpperToLower
    val parameterCasters: MutableMap<KClass<out Any>, ParameterCaster<out Any>> = mutableMapOf(
        Pair(String::class, StringCaster()),
        Pair(Boolean::class, BooleanCaster()),
        Pair(Char::class, CharCaster()),
        Pair(Date::class, DateCaster())
    )
}