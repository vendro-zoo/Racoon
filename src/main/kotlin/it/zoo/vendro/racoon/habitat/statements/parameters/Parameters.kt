package it.zoo.vendro.racoon.habitat.statements.parameters

import it.zoo.vendro.racoon.habitat.ConnectionManager
import it.zoo.vendro.racoon.habitat.configuration.RacoonConfiguration
import it.zoo.vendro.racoon.habitat.context.ToParameterCasterContext
import it.zoo.vendro.racoon.habitat.definition.ColumnInsertion
import java.sql.PreparedStatement

class Parameters(val manager: ConnectionManager) {
    private val config: RacoonConfiguration
        get() = manager.pool.configuration

    // Query parameters
    val indexedParameters: MutableMap<Int, ParameterFieldValue<*>> = mutableMapOf()
    val namedParameters: MutableMap<String, ParameterFieldValue<*>> = mutableMapOf()

    /**
     * Sets the value of an indexed parameter of the query.
     *
     * @param index The index of the parameter.
     * @param value The value of the parameter.
     */
    fun <T : Any> setParam(index: Int, value: T?, columnInsertion: ColumnInsertion? = null) =
        addParameterToMap(indexedParameters, index, value, columnInsertion ?: ColumnInsertion.Object)

    /**
     * Sets the value of a named parameter of the query.
     *
     * @param name The name of the parameter.
     * @param value The value of the parameter.
     */
    fun <T : Any> setParam(name: String, value: T?, columnInsertion: ColumnInsertion? = null) =
        addParameterToMap(namedParameters, name, value, columnInsertion ?: ColumnInsertion.Object)


    private fun <T, K : Any> addParameterToMap(
        map: MutableMap<T, ParameterFieldValue<*>>,
        index: T,
        value: K?,
        columnInsertion: ColumnInsertion
    ) {
        if (value == null) {
            map[index] = ParameterFieldValue(
                value = null,
                columnInsertion = columnInsertion
            )
            return
        }

        val caster = config.casting.getFirstCaster(value::class)

        map[index] = ParameterFieldValue(
            value = if (caster == null) value else caster.toQuery(
                value,
                ToParameterCasterContext(manager, value::class)
            ),
            columnInsertion = columnInsertion
        )
    }

    fun bind(preparedStatement: PreparedStatement, mapping: ParameterMapping) {
        indexedParameters.forEach {
            val value = it.value.value
            val inserter = it.value.columnInsertion
            if (value is List<*>) {
                value.withIndex().map { v ->
                    val s = "racoon_internal_ip_${it.key}_${v.index}"
                    val realIndex = mapping.getNamed(s)
                    inserter.insert(preparedStatement, realIndex, v.value)
                }
            } else {
                val realIndex = mapping.getIndexed(it.key)
                inserter.insert(preparedStatement, realIndex, value)
            }
        }

        namedParameters.forEach {
            val value = it.value.value
            val inserter = it.value.columnInsertion
            if (value is List<*>) {
                value.withIndex().map { v ->
                    val s = "racoon_internal_ni_${it.key}_${v.index}"
                    val realIndex = mapping.getNamed(s)
                    inserter.insert(preparedStatement, realIndex, v.value)
                }
            } else {
                val realIndex = mapping.getNamed(it.key)
                inserter.insert(preparedStatement, realIndex, value)
            }
        }
    }
}

data class ParameterFieldValue<T>(
    val value: T?,
    val columnInsertion: ColumnInsertion
)