package it.zoo.vendro.racoon.statements

import it.zoo.vendro.racoon.definition.ColumnInsertion
import it.zoo.vendro.racoon.connection.ConnectionManager
import it.zoo.vendro.racoon.statements.parameters.ParameterMapping
import it.zoo.vendro.racoon.statements.parameters.Parameters
import java.sql.PreparedStatement
import java.sql.SQLException
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * An abstract class that represents a racoon.
 *
 * Handles the basic functionality of all racoons such as parameter handling.
 * @param R The subclass of [Statement].
 * @param manager The [ConnectionManager] that manages this racoon.
 * @param originalQuery The original query that was used to create this racoon.
 */
abstract class Statement<R : Statement<R>>(val manager: ConnectionManager, val originalQuery: String) : AutoCloseable {
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
        val preparedStatement = preparedStatement
            ?: throw IllegalStateException("A prepared statement must be set before binding parameters.")

        val parameterMapping =
            parameterMapping ?: throw IllegalStateException("Parameter mappings must be set before binding parameters.")

        parameters.bind(preparedStatement, parameterMapping)
    }


    /**
     * Sets the value of an indexed parameter of the query.
     *
     * @param index The index of the parameter.
     * @param value The value of the parameter.
     *
     * @return The [QueryStatement] instance.
     */
    inline fun <reified T> setParam(
        index: Int,
        value: T?,
        columnInsertion: ColumnInsertion = ColumnInsertion.Object
    ): R =
        setParamK(index, value, typeOf<T>(), columnInsertion)

    fun <T> setParamK(
        index: Int,
        value: T?,
        tType: KType,
        columnInsertion: ColumnInsertion = ColumnInsertion.Object
    ): R {
        parameters.setParamK(index, value, tType, columnInsertion)
        return self()
    }

    /**
     * Sets the value of a named parameter of the query.
     *
     * @param name The name of the parameter.
     * @param value The value of the parameter.
     *
     * @return The [QueryStatement] instance.
     */
    inline fun <reified T> setParam(
        name: String,
        value: T?,
        columnInsertion: ColumnInsertion = ColumnInsertion.Object
    ): R =
        setParamK(name, value, typeOf<T>(), columnInsertion)

    fun <T> setParamK(
        name: String,
        value: T?,
        tType: KType,
        columnInsertion: ColumnInsertion = ColumnInsertion.Object
    ): R {
        parameters.setParamK(name, value, tType, columnInsertion)
        return self()
    }


    /**
     * Closes the [Statement] instance.
     * This method should be called when the [Statement] instance is no longer needed.
     */
    override fun close() {
        preparedStatement?.close()
    }


    /**
     * A reference to the subclassed [Statement] instance.
     * @return The [Statement] instance cast to the type of the subclass.
     */
    @Suppress("UNCHECKED_CAST")
    fun self(): R = this as R
}