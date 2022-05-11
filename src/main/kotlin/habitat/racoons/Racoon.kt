package habitat.racoons

import commons.casting.ParameterCaster
import commons.configuration.RacoonConfiguration
import habitat.RacoonManager
import java.sql.PreparedStatement
import java.sql.SQLException

/**
 * An abstract class that represents a racoon.
 *
 * Handles the basic functionality of all racoons such as parameter handling.
 * @param R The subclass of [Racoon].
 * @param manager The [RacoonManager] that manages this racoon.
 * @param originalQuery The original query that was used to create this racoon.
 */
abstract class Racoon<R: Racoon<R>>(val manager: RacoonManager, val originalQuery: String) : AutoCloseable {
    // Prepared statement
    var preparedStatement: PreparedStatement? = null
    var processedQuery: String? = null

    // Parameters mappings
    var indexedParametersMappings: Map<Int, Int>? = null
    var namedParametersMappings: Map<String, Int>? = null

    // Query parameters
    private val indexedParameters: MutableMap<Int, Any> = mutableMapOf()
    private val namedParameters: MutableMap<String, Any> = mutableMapOf()

    abstract fun execute(): R

    /**
     * Uses the mappings and the parameters to set the values in the prepared statement.
     * @throws SQLException If an error occurs while setting the parameters.
     * @throws IllegalStateException If the prepared statement or the parameter mappings are not set.
     */
    fun bindParameters() {
        // Checking if prepared statement and the mappings are set
        val preparedStatement = preparedStatement ?:
            throw IllegalStateException("A prepared statement must be set before binding parameters.")

        val indexedParametersMappings = indexedParametersMappings ?:
            throw IllegalStateException("Indexed parameters mappings must be set before binding parameters.")

        val namedParametersMappings = namedParametersMappings ?:
            throw IllegalStateException("Named parameters mappings must be set before binding parameters.")


        // Binding the indexed parameters
        indexedParameters.forEach{
            val realIndex = indexedParametersMappings[it.key] ?:
            throw SQLException("Indexed parameter ${it.key} not found")

            preparedStatement.setObject(realIndex, it.value)
        }

        // Binding the named parameters
        namedParameters.forEach{
            val realIndex = namedParametersMappings[it.key] ?:
            throw SQLException("Named parameter ${it.key} not found")

            preparedStatement.setObject(realIndex, it.value)
        }
    }


    /**
     * Sets an indexed parameter of the query.
     *
     * @param index The index of the parameter.
     * @param value The value of the parameter.
     *
     * @return The [QueryRacoon] instance.
     */
    fun <T : Any> setParam(index: Int, value: T): R {
        val caster = RacoonConfiguration.Casting.getCaster(value::class)

        @Suppress("UNCHECKED_CAST")
        indexedParameters[index] = (caster as ParameterCaster<Any, Any>?)?.cast(value) ?: value

        return self()
    }

    /**
     * Sets a named parameter of the query.
     *
     * @param name The name of the parameter.
     * @param value The value of the parameter.
     *
     * @return The [QueryRacoon] instance.
     */
    fun <T : Any> setParam(name: String, value: T): R {
        val caster = RacoonConfiguration.Casting.getCaster(value::class)

        @Suppress("UNCHECKED_CAST")
        namedParameters[name] = (caster as ParameterCaster<Any, Any>?)?.cast(value) ?: value

        return self()
    }


    /**
     * Closes the [Racoon] instance.
     * This method should be called when the [Racoon] instance is no longer needed.
     */
    override fun close() {
        preparedStatement?.close()
    }


    /**
     * A reference to the subclassed [Racoon] instance.
     * @return The [Racoon] instance cast to the type of the subclass.
     */
    @Suppress("UNCHECKED_CAST")
    fun self(): R = this as R
}