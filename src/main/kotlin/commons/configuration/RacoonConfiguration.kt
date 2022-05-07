package commons.configuration

import commons.TableAliasMappers
import commons.casting.ParameterCaster
import commons.casting.casters.BooleanCaster
import commons.casting.casters.CharCaster
import commons.casting.casters.DateCaster
import commons.casting.casters.StringCaster
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