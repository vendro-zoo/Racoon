package habitat.racoons.parameters

import habitat.RacoonManager
import habitat.configuration.RacoonConfiguration
import habitat.context.ToParameterCasterContext
import java.sql.PreparedStatement

class Parameters(val manager: RacoonManager) {
    // Query parameters
    private val indexedParameters: MutableMap<Int, Any?> = mutableMapOf()
    private val namedParameters: MutableMap<String, Any?> = mutableMapOf()

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

        val caster = RacoonConfiguration.Casting.getCaster(value::class)

        @Suppress("UNCHECKED_CAST")
        map[index] = if (caster == null) value else caster.toQuery(value, ToParameterCasterContext(manager, value::class))
    }

    fun bind(preparedStatement: PreparedStatement, mapping: ParameterMapping) {
        indexedParameters.forEach{
            val realIndex = mapping.getIndexed(it.key)
            preparedStatement.setObject(realIndex, it.value)
        }

        namedParameters.forEach{
            val realIndex = mapping.getNamed(it.key)
            preparedStatement.setObject(realIndex, it.value)
        }
    }
}