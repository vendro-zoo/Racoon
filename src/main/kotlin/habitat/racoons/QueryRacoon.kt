package habitat.racoons

import habitat.RacoonManager
import habitat.configuration.RacoonConfiguration
import habitat.context.FromParameterCasterContext
import habitat.definition.ColumnName
import habitat.definition.TableName
import internals.casting.castEquivalent
import internals.extensions.asKClass
import internals.extensions.isMarkedNullable
import internals.extensions.isNullOrOptional
import internals.query.QueryProcessing
import java.sql.ResultSet
import java.sql.SQLException
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

@Suppress("unused")
class QueryRacoon(
    manager: RacoonManager,
    originalQuery: String,
) : Racoon<QueryRacoon>(manager, originalQuery) {
    private var resultSet: ResultSet? = null
    private val tableAliases: MutableMap<KClass<*>, String> = mutableMapOf()

    /**
     * Adds a table alias to be used when mapping the result of the query to a class.
     *
     * This overrides any annotation and defaults mapping from the class to a table.
     *
     * If the query contains the same table multiple times,
     * the alias must be re-set before each mapping.
     *
     * @param clazz The class to alias.
     * @param alias The alias to use.
     */
    fun setAlias(clazz: KClass<*>, alias: String) = apply { tableAliases[clazz] = alias }

    /**
     * Processes the query and saves the result in the [QueryRacoon].
     *
     * In more details, the query is first processed by binding the parameters, and then the query is executed.
     *
     * @return the [QueryRacoon] itself
     */
    override fun execute() = apply {
        val queryProcessingResult = QueryProcessing.reconstructQuery(originalQuery, parameters)

        val processedQuery = queryProcessingResult.first
        parameterMapping = queryProcessingResult.second

        preparedStatement = manager.prepareScrollable(processedQuery)

        bindParameters()

        resultSet = preparedStatement?.executeQuery()
    }

    /**
     * Returns the number of columns in the result set.
     *
     * @return the number of columns in the result set or `null` if the result set has not been created yet.
     */
    fun countColumns() = resultSet?.metaData?.columnCount

    /**
     * Returns the number of rows in the result set.
     *
     * This method goes to the last row of the result set and returns its row number.
     * This operation may be expensive, depending on the size of the result set.
     *
     * A better way to get the number of rows is to use the `COUNT(*)` SQL function.
     *
     * @return the number of rows in the result set or `null` if the result set has not been created yet.
     */
    fun countRows() = resultSet?.let {
        it.last()
        val rowCount = it.row
        it.beforeFirst()
        rowCount
    }

    /**
     * Behaves like [mapToClassK], but instead of passing the class as a normal parameter, it is passed as a reified type.
     *
     * @see mapToClassK
     */
    inline fun <reified T : Any> mapToClass(): List<T> = mapToClassK(T::class)

    /**
     * Maps the result of the query to a class.
     *
     * If the query has not been executed yet, it is executed first.
     *
     * @param tClass The class to map to.
     *
     * @return A list of [T] containing the result of the mapping.
     * @throws ClassCastException If an error occurs during the mapping.
     * See the message of the exception for more details.
     */
    fun <T : Any> mapToClassK(tClass: KClass<T>): List<T> {
        @Suppress("UNCHECKED_CAST")
        return mapToNullableClassK(tClass) as List<T>
    }

    /**
     * Maps the result of the query to a class.
     *
     * If the query has not been executed yet, it is executed first.
     *
     * An optional parameter `nullable` can be used to specify if the result of the mapping can contain `null` values.
     *
     * @param tClass The class to map to.
     * @param nullable If `true`, the result of the mapping can contain `null` values. The default value is `false`.
     * @return A list of [T] containing the result of the mapping.
     * @throws ClassCastException If an error occurs during the mapping. See the message of the exception for more details.
     */
    @Suppress("kotlin:S3776")
    fun <T : Any> mapToNullableClassK(tClass: KClass<T>, nullable: Boolean = false): List<T?> {
        // If the query has not been executed yet, execute it
        resultSet ?: execute()
        val immutableResultSet = resultSet!!

        // Get the class of the type we want to map to
        val clazzName = tClass.simpleName ?: throw ClassCastException("Class name is null")

        // Get the table alias for the class specified in either the racoon or the class
        // If not specified, generate an alias
        val sqlAlias = tableAliases[tClass] ?: TableName.getAlias(tClass)

        // Get the primary constructor of the class and its parameters
        val constructor = tClass.primaryConstructor ?: throw ClassCastException("$clazzName has no primary constructor")
        val parameters = constructor.parameters

        // The list to be returned
        val list = mutableListOf<T?>()

        // Reset the result set pointer to the beginning
        immutableResultSet.beforeFirst()

        // For each record in the result set
        rsWhile@while (immutableResultSet.next()) {
            // Create an empty map for the parameters of the constructor
            val map: MutableMap<KParameter, Any?> = mutableMapOf()

            // For each parameter of the constructor
            rsFor@for (parameter in parameters) {
                // Get the column name
                val name = ColumnName.getName(parameter)

                // Get the column type
                val kClass = parameter.asKClass()
                val kType = parameter.type

                // Getting the value from the result set
                var value: Any? = getResultSetValue(immutableResultSet, "$sqlAlias.$name")
                    ?: getResultSetValue(immutableResultSet, name)
                    ?: getResultSetValue(immutableResultSet, "${sqlAlias}_$name")
                ?: if (parameter.isMarkedNullable()) {
                    // If no value is found and the parameter is nullable, set it to null
                    map[parameter] = null
                    continue@rsFor
                }
                else if (!parameter.isOptional) {
                    // If the parameter is not nullable and is not optional
                    if (nullable) {
                        // If the list can contain null values, insert a null and skip to the next record
                        list.add(null)
                        continue@rsWhile
                    }
                    // If the list cannot contain null values, throw an exception
                    else throw ClassCastException("Can't map $clazzName to $tClass because $name is null")
                // Else if it is optional, continue to the next parameter
                } else continue@rsFor

                // Getting the user defined type caster, if it exists
                val caster = RacoonConfiguration.Casting.getCaster(kClass)

                // Casting with the user defined type caster, otherwise casting with the internal caster
                value = caster?.fromQuery(value!!, FromParameterCasterContext(manager, kType))
                    ?: castEquivalent(parameter, value!!)

                // Set the value as the constructor parameter
                map[parameter] = value
            }
            // Create the object from the map and add it to the list
            list.add(constructor.callBy(map))
        }

        // Return the list
        return list.toList()
    }

    /**
     * Maps the result of the query to a wrapper class.
     *
     * Each property of the wrapper class is mapped to the result of [mapToClassK].
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
            try {
                mapToNullableClassK(it.asKClass(), it.isNullOrOptional())
            } catch (e: ClassCastException) {
                throw ClassCastException("An exception occurred while mapping ${it.asKClass().simpleName}. " +
                        "Did you forget to make the property nullable?\n" +
                        "The exception's message: ${e.message}")
            }
        }.map {
            // Convert map of lists to list of maps
            it.value.map { i -> it.key to i }

            // Creates a matrix of the parameters and their values
        }.withIndex().fold(mutableListOf<Array<Pair<KParameter, Any?>?>>()) { acc, (i, v) ->
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

    inline fun <reified T: Number> mapToNumber() = mapToNumberK(T::class)

    /**
     * Maps the result of the query to the specified number class.
     *
     * Only the first column of each record is mapped to the number class.
     *
     * @param T The wrapper class to map to.
     * @return A list of [T] containing the result of the mapping.
     * @throws ClassCastException If an error occurs during the mapping.
     */
    fun <T: Number> mapToNumberK(kClass: KClass<T>): List<T?> {
        // If the query has not been executed yet, execute it
        resultSet ?: execute()
        val immutableResultSet = resultSet!!

        // Create the list to then return
        val list = mutableListOf<T?>()

        // For each record in the result set
        while (immutableResultSet.next()) {
            // Map the first column of the result set to the number class
            @Suppress("UNCHECKED_CAST", "KotlinRedundantDiagnosticSuppress")
            list.add(when (kClass) {
                Int::class -> numberOrNull(immutableResultSet, immutableResultSet.getInt(1) as T)
                Long::class -> numberOrNull(immutableResultSet, immutableResultSet.getLong(1) as T)
                Short::class -> numberOrNull(immutableResultSet, immutableResultSet.getShort(1) as T)
                Byte::class -> numberOrNull(immutableResultSet, immutableResultSet.getByte(1) as T)
                Float::class -> numberOrNull(immutableResultSet, immutableResultSet.getFloat(1) as T)
                Double::class -> numberOrNull(immutableResultSet, immutableResultSet.getDouble(1) as T)
                else -> throw ClassCastException("Can't map to $kClass")
            })
        }

        // Return the list
        return list
    }

    private fun <T: Number> numberOrNull(rs: ResultSet, value: T): T? =
        if (value.toInt() == 0 && rs.wasNull()) null else value

    fun mapToString(): List<String> {
        // If the query has not been executed yet, execute it
        resultSet ?: execute()
        val immutableResultSet = resultSet!!

        // Create the list to then return
        val list = mutableListOf<String>()

        // For each record in the result set, map the first column to a string
        while (immutableResultSet.next()) list.add(immutableResultSet.getString(1))

        // Return the list
        return list
    }

    fun <T> mapToCustom(fn: (ResultSet) -> T): List<T> {
        // If the query has not been executed yet, execute it
        resultSet ?: execute()
        val immutableResultSet = resultSet!!

        // Create the list to then return
        val res = mutableListOf<T>()

        // For each record in the result set, map the first column by using the provided function
        while (immutableResultSet.next()) res.add(fn(immutableResultSet))

        // Return the list
        return res
    }

    override fun close() {
        // Close the result set
        resultSet?.close()

        // Close the statement
        super.close()
    }

    companion object {
        private fun getResultSetValue(resultSet: ResultSet, columnName: String): Any? {
            return try { resultSet.getObject(columnName) } catch (_: SQLException) { null }
        }
    }
}