package habitat.racoons

import habitat.RacoonManager
import habitat.racoons.parameters.ParameterMapping
import habitat.racoons.parameters.Parameters
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
    var parameterMapping: ParameterMapping? = null

    // Query parameters
    val parameters: Parameters = Parameters(manager)

    /**
     * Executes the query to the database.
     *
     * Calling this function may change the state of the subclass.
     */
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

        val parameterMapping = parameterMapping ?:
            throw IllegalStateException("Parameter mappings must be set before binding parameters.")

        parameters.bind(preparedStatement, parameterMapping)
    }


    /**
     * Sets the value of an indexed parameter of the query.
     *
     * @param index The index of the parameter.
     * @param value The value of the parameter.
     *
     * @return The [QueryRacoon] instance.
     */
    fun <T : Any> setParam(index: Int, value: T?): R {
        parameters.setParam(index, value)
        return self()
    }

    /**
     * Sets the value of a named parameter of the query.
     *
     * @param name The name of the parameter.
     * @param value The value of the parameter.
     *
     * @return The [QueryRacoon] instance.
     */
    fun <T : Any> setParam(name: String, value: T?): R {
        parameters.setParam(name, value)
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