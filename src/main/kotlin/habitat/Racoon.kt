package habitat

import commons.casting.ParameterCaster
import commons.configuration.RacoonConfiguration
import commons.query.QueryProcessing
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

@Suppress("unused")
class Racoon(
    // Mandatory parameters
    private val manager: RacoonManager,
    private val originalQuery: String,
    private val type: RacoonType,

    // Query processing results
    private var preparedStatement: PreparedStatement? = null,
    private var resultSet: ResultSet? = null,

    // Query parameters
    private val indexedParameters: MutableMap<Int, Any> = mutableMapOf(),
    private val namedParameters: MutableMap<String, Any> = mutableMapOf(),

    // Mapping and aliases
    private val tableAliases: MutableMap<KClass<*>, String> = mutableMapOf(),
    private var indexedParametersMappings: Map<Int, Int>? = null,
    private var namedParametersMappings: Map<String, Int>? = null,
) : AutoCloseable {
    /**
     * Adds a table alias to be used when mapping the result of the query to a class.
     *
     * If the query contains the same table multiple times,
     * the alias must be re-set before each mapping.
     *
     * @param clazz The class to alias.
     * @param alias The alias to use.
     */
    fun setAlias(clazz: KClass<*>, alias: String) = apply { tableAliases[clazz] = alias }

    /**
     * Executes the query and save the result in the [Racoon].
     *
     * @return the [Racoon] itself
     */
    fun execute() = apply {
        val queryProcessingResult = QueryProcessing.reconstructQuery(originalQuery)

        val processedQuery = queryProcessingResult.first
        indexedParametersMappings = queryProcessingResult.second
        namedParametersMappings = queryProcessingResult.third

        preparedStatement = manager.prepare(processedQuery)

        bindParameters()

        when (type) {
            RacoonType.QUERY -> {
                resultSet = preparedStatement?.executeQuery()
            }
            else -> {
                TODO("Not yet implemented")
            }
        }
    }

    private fun bindParameters() {
        indexedParameters.forEach{
            val realIndex = indexedParametersMappings!![it.key] ?:
                throw SQLException("Indexed parameter ${it.key} not found")

            preparedStatement!!.setObject(realIndex, it.value)
        }

        namedParameters.forEach{
            val realIndex = namedParametersMappings!![it.key] ?:
                throw SQLException("Named parameter ${it.key} not found")

            preparedStatement!!.setObject(realIndex, it.value)
        }
    }

    /**
     * Maps the result of the query to a class.
     *
     * @param T The class to map to.
     *
     * @return A list of [T] containing the result of the mapping.
     * @throws ClassCastException If an error occurs during the mapping.
     * See the message of the exception for more details.
     */
    inline fun <reified T : Any> mapToClass(): List<T> = mapToClass(T::class)

    /**
     * Maps the result of the query to a class.
     *
     * @param T The class to map to.
     *
     * @return A list of [T] containing the result of the mapping.
     * @throws ClassCastException If an error occurs during the mapping.
     * See the message of the exception for more details.
     */
    fun <T : Any> mapToClass(tClass: KClass<T>): List<T> {
        // If the query has not been executed yet, execute it
        resultSet ?: execute()
        val immutableResultSet = resultSet!!

        // Get the class of the type we want to map to
        val clazzName = tClass.simpleName ?: throw ClassCastException("Class name is null")

        // Get the table alias for the class or generate one if it isn't specified
        val sqlAlias = tableAliases[tClass] ?: RacoonConfiguration.TableAliases.getAlias(clazzName)

        // Get the primary constructor of the class and its parameters
        val constructor = tClass.primaryConstructor ?: throw ClassCastException("$clazzName has no primary constructor")
        val parameters = constructor.parameters

        // The list to be returned
        val list = mutableListOf<T>()

        // Reset the result set pointer to the beginning
        immutableResultSet.beforeFirst()

        // Loop through the result set
        while (immutableResultSet.next()) {
            // Create a map of the column names to their values
            val map: Map<KParameter, Any?> = parameters.associateWith {
                // Get the name of the parameter
                val name = it.name ?: throw ClassCastException("Can't access a property because it's name is null")

                try {
                    // Try to get the column of the resultSet with the table alias and the parameter name
                    immutableResultSet.getObject("$sqlAlias.$name")
                } catch (e: SQLException) {
                    try {
                        // Try to get the column of the resultSet with only the parameter name
                        immutableResultSet.getObject(name)
                    } catch (e: SQLException) {
                        // If the column doesn't exist and the parameter is not optional, throw an exception
                        if (!it.isOptional)
                            throw ClassCastException("resultSet has no column '$sqlAlias.$name' or '$name'")
                        else null
                    }
                }
            }.filter { it.value != null }.toMap()

            // Create a new instance of the class and add it to the list
            list.add(constructor.callBy(map))
        }

        // Return the list
        return list.toList()
    }

    /**
     * Maps the result of the query to a wrapper class.
     *
     * Each property of the wrapper class is mapped to the result of [mapToClass].
     *
     * @param T The wrapper class to map to.
     *
     * @return A list of [T] containing the result of the mapping.
     * @throws ClassCastException If an error occurs during the mapping.
     */
    inline fun <reified T : Any> multiMapToClass(): List<T> {
        // Get the class containing the properties to map to
        val clazz = T::class

        // Get the primary constructor of the class and its parameters
        val constructor =
            clazz.primaryConstructor ?: throw ClassCastException("${clazz.simpleName} has no primary constructor")
        val parameters = constructor.parameters
        val paramSize = parameters.size

        val listOfWrappers = parameters.associateWith {
            mapToClass(it.type.classifier as KClass<*>)
        }.map {
            // Convert map of lists to list of maps
            it.value.map { i -> it.key to i }

            // Creates a matrix of the parameters and their values
        }.withIndex().fold(mutableListOf<Array<Pair<KParameter, Any>?>>()) { acc, (i, v) ->
            // Fill the matrix with the values
            v.withIndex().forEach { (index, value) ->
                // Add an item to the first layer of the matrix if the size is less than the current index
                if (index > acc.size - 1) acc.add((0 until paramSize).map { null }.toTypedArray())

                // For the first layer, add the value to the current index of the list of pairs
                // For the second layer, add the value to the current index of the parameter
                acc[index][i] = value
            }
            acc
        }.map {
            // Cast to not nullable pairs
            @Suppress("UNCHECKED_CAST")
            it as Array<Pair<KParameter, Any>>
        }.map {
            // Creates a wrapper instance for each item in the list of maps
            constructor.callBy(it.toMap())
        }

        // Return the list of wrappers
        return listOfWrappers
    }

    /**
     * Sets an indexed parameter of the query.
     *
     * @param index The index of the parameter.
     * @param value The value of the parameter.
     *
     * @return The [Racoon] instance.
     * @throws NoSuchMethodException if a [ParameterCaster] is not found for the parameter's type.
     */
    fun <T : Any> setParam(index: Int, value: T): Racoon = apply {
        val caster = RacoonConfiguration.Casting.getCaster(value::class)

        @Suppress("UNCHECKED_CAST")
        indexedParameters[index] = (caster as ParameterCaster<Any, Any>?)?.cast(value) ?: value
    }

    /**
     * Sets a named parameter of the query.
     *
     * @param name The name of the parameter.
     * @param value The value of the parameter.
     *
     * @return The [Racoon] instance.
     * @throws NoSuchMethodException if a [ParameterCaster] is not found for the parameter's type.
     */
    fun <T : Any> setParam(name: String, value: T): Racoon = apply {
        val caster = RacoonConfiguration.Casting.getCaster(value::class)

        @Suppress("UNCHECKED_CAST")
        namedParameters[name] = (caster as ParameterCaster<Any, Any>?)?.cast(value) ?: value
    }

    /**
     * Closes the [Racoon] instance.
     * This method should be called when the [Racoon] instance is no longer needed.
     */
    override fun close() {
        resultSet?.close()
        preparedStatement?.close()
    }
}