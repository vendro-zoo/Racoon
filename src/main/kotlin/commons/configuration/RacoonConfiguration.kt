package commons.configuration

import commons.TableAliasMappers
import commons.casting.ParameterCaster
import commons.casting.casters.*
import java.util.*
import kotlin.reflect.KClass

object RacoonConfiguration {
    var defaultConnectionSettings: ConnectionSettings? = null
    var defaultTableAliasMapper: (String) -> String = TableAliasMappers.onlyUpperToLower
    val parameterCasters: MutableMap<KClass<out Any>, ParameterCaster<out Any>> = mutableMapOf(
        Pair(String::class, StringCaster()),
        Pair(Boolean::class, BooleanCaster()),
        Pair(Char::class, CharCaster()),
        Pair(Date::class, DateCaster()),
        Pair(Number::class, NumberCaster())
    )
}