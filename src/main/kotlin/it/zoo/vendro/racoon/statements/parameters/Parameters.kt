package it.zoo.vendro.racoon.statements.parameters

import it.zoo.vendro.racoon.configuration.RacoonConfiguration
import it.zoo.vendro.racoon.context.ToQueryCasterContext
import it.zoo.vendro.racoon.definition.ColumnInsertion
import it.zoo.vendro.racoon.connection.ConnectionManager
import java.sql.PreparedStatement
import kotlin.reflect.KType
import kotlin.reflect.typeOf

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
    inline fun <reified T> setParam(index: Int, value: T?, columnInsertion: ColumnInsertion? = null) =
        setParamK(index, value, typeOf<T>(), columnInsertion)

    /**
     * Sets the value of a named parameter of the query.
     *
     * @param name The name of the parameter.
     * @param value The value of the parameter.
     */
    inline fun <reified T> setParam(name: String, value: T?, columnInsertion: ColumnInsertion? = null) =
        setParamK(name, value, typeOf<T>(), columnInsertion)

    fun <T> setParamK(index: Int, value: T?, tType: KType, columnInsertion: ColumnInsertion? = null) =
        addParameterToMap(indexedParameters, index, value, tType, columnInsertion ?: ColumnInsertion.Object)

    fun <T> setParamK(name: String, value: T?, tType: KType, columnInsertion: ColumnInsertion? = null) =
        addParameterToMap(namedParameters, name, value, tType, columnInsertion ?: ColumnInsertion.Object)

    @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
    private fun <T, K> addParameterToMap(
        map: MutableMap<T, ParameterFieldValue<*>>,
        index: T,
        value: K?,
        kType: KType,
        columnInsertion: ColumnInsertion
    ) {
        if (value == null) {
            map[index] = ParameterFieldValue(
                value = null,
                columnInsertion = columnInsertion
            )
            return
        }

        val notNullValue = value!!

        val caster = config.casting.getFirstCasterK(kType)

        map[index] = ParameterFieldValue(
            value = if (caster == null) value else caster.toQuery(
                notNullValue,
                ToQueryCasterContext(manager, notNullValue::class)
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