package it.zoo.vendro.racoon.habitat.racoons.parameters

import it.zoo.vendro.racoon.habitat.RacoonManager
import it.zoo.vendro.racoon.habitat.configuration.RacoonConfiguration
import it.zoo.vendro.racoon.habitat.context.ToParameterCasterContext
import java.sql.PreparedStatement

class Parameters(val manager: RacoonManager) {
    // Query parameters
    val indexedParameters: MutableMap<Int, Any?> = mutableMapOf()
    val namedParameters: MutableMap<String, Any?> = mutableMapOf()

    /**
     * Sets the value of an indexed parameter of the query.
     *
     * @param index The index of the parameter.
     * @param value The value of the parameter.
     */
    fun <T : Any> setParam(index: Int, value: T?) = setParam(indexedParameters, index, value)

    /**
     * Sets the value of a named parameter of the query.
     *
     * @param name The name of the parameter.
     * @param value The value of the parameter.
     */
    fun <T : Any> setParam(name: String, value: T?) = setParam(namedParameters, name, value)


    private fun <T, K: Any> setParam(map: MutableMap<T, Any?>, index: T, value: K?) {
        if (value == null) {
            map[index] = null
            return
        }

        val caster = RacoonConfiguration.Casting.getFirstCaster(value::class)

        map[index] = if (caster == null) value else caster.toQuery(value, ToParameterCasterContext(manager, value::class))
    }

    fun bind(preparedStatement: PreparedStatement, mapping: ParameterMapping) {
        indexedParameters.forEach{
            val value = it.value
            if (value is List<*>) {
                value.withIndex().map { v ->
                    val s = "racoon_internal_ip_${it.key}_${v.index}"
                    val realIndex = mapping.getNamed(s)
                    preparedStatement.setObject(realIndex, v.value)
                }
            } else {
                val realIndex =  mapping.getIndexed(it.key)
                preparedStatement.setObject(realIndex, it.value)
            }
        }

        namedParameters.forEach{
            val value = it.value
            if (value is List<*>) {
                value.withIndex().map { v ->
                    val s = "racoon_internal_ni_${it.key}_${v.index}"
                    val realIndex = mapping.getNamed(s)
                    preparedStatement.setObject(realIndex, v.value)
                }
            } else {
                val realIndex = mapping.getNamed(it.key)
                preparedStatement.setObject(realIndex, it.value)
            }
        }
    }
}