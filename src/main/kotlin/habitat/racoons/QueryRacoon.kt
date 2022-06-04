package habitat.racoons

import habitat.RacoonManager
import habitat.configuration.RacoonConfiguration
import habitat.context.ParameterCasterContext
import habitat.definition.ColumnName
import habitat.definition.LazyId
import habitat.definition.Table
import habitat.definition.TableName
import internals.casting.castEquivalent
import internals.expansions.asKClass
import internals.expansions.getRuntimeGeneric
import internals.expansions.isMarkedNullable
import internals.expansions.isNullOrOptional
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
) : Racoon<QueryRacoon>(manager, originalQuery), AutoCloseable {
    private var resultSet: ResultSet? = null
    private val tableAliases: MutableMap<KClass<*>, String> = mutableMapOf()

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
     * Processes the query and saves the result in the [QueryRacoon].
     *
     * In more details, the query is first processed by binding the parameters, and then the query is executed.
     *
     * @return the [QueryRacoon] itself
     */
    override fun execute() = apply {
        val queryProcessingResult = QueryProcessing.reconstructQuery(originalQuery)

        val processedQuery = queryProcessingResult.first
        indexedParametersMappings = queryProcessingResult.second
        namedParametersMappings = queryProcessingResult.third

        preparedStatement = manager.prepare(processedQuery)

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
     * Maps the result of the query to a class.
     *
     * If the query has not been executed yet, it is executed first.
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
     * If the query has not been executed yet, it is executed first.
     *
     * @param T The class to map to.
     *
     * @return A list of [T] containing the result of the mapping.
     * @throws ClassCastException If an error occurs during the mapping.
     * See the message of the exception for more details.
     */
    fun <T : Any> mapToClass(tClass: KClass<T>): List<T> {
        @Suppress("UNCHECKED_CAST")
        return mapToNullableClass(tClass) as List<T>
    }

    @Suppress("kotlin:S3776")
    fun <T : Any> mapToNullableClass(tClass: KClass<T>, nullable: Boolean = false): List<T?> {
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

        rsWhile@while (immutableResultSet.next()) {
            val map: MutableMap<KParameter, Any?> = mutableMapOf()

            rsFor@for (parameter in parameters) {
                val name = ColumnName.getName(parameter)

                val kClass = parameter.asKClass()

                val isLazy = kClass == LazyId::class
                @Suppress("UNCHECKED_CAST")
                val kGeneric: KClass<Table>? = if(isLazy) parameter.getRuntimeGeneric() as KClass<Table> else null

                var value: Any? = getResultSetValue(immutableResultSet, "$sqlAlias.$name") ?:
                    getResultSetValue(immutableResultSet, name)
                ?: if (parameter.isMarkedNullable()) {
                    map[parameter] = null
                    continue@rsFor
                }
                else if (!parameter.isOptional) {
                    if (nullable) {
                        list.add(null)
                        continue@rsWhile
                    }
                    else throw ClassCastException("Can't map $clazzName to $tClass because $name is null")
                } else continue@rsFor

                // Getting the user defined type [ParameterCaster], if it exists
                val caster = RacoonConfiguration.Casting.getCaster(kClass)

                val kActual = kGeneric ?: kClass

                // Casting with the user defined type [ParameterCaster], otherwise casting with the internal caster
                value = caster?.fromQuery(value!!, ParameterCasterContext(manager, kActual))
                    ?: castEquivalent(parameter, value!!)

                map[parameter] = value
            }
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
            try {
                mapToNullableClass(it.asKClass(), it.isNullOrOptional())
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

    inline fun <reified T: Number> mapToNumber() = mapToNumber(T::class)

    fun <T: Number> mapToNumber(kClass: KClass<T>): List<T> {
        resultSet ?: execute()
        val immutableResultSet = resultSet!!

        val list = mutableListOf<T>()
        while (immutableResultSet.next()) {
            @Suppress("UNCHECKED_CAST", "KotlinRedundantDiagnosticSuppress")
            list.add(when (kClass) {
                Int::class -> immutableResultSet.getInt(1) as T
                Long::class -> immutableResultSet.getLong(1) as T
                Short::class -> immutableResultSet.getShort(1) as T
                Byte::class -> immutableResultSet.getByte(1) as T
                Float::class -> immutableResultSet.getFloat(1) as T
                Double::class -> immutableResultSet.getDouble(1) as T
                else -> throw ClassCastException("Can't map to $kClass")
            })
        }
        return list
    }

    fun mapToString(): List<String> {
        resultSet ?: execute()
        val immutableResultSet = resultSet!!

        val list = mutableListOf<String>()
        while (immutableResultSet.next()) list.add(immutableResultSet.getString(1))
        return list
    }

    fun <T> mapToCustom(fn: (ResultSet) -> T): List<T> {
        resultSet ?: execute()
        val immutableResultSet = resultSet!!

        val res = mutableListOf<T>()
        while (immutableResultSet.next()) res.add(fn(immutableResultSet))

        return res
    }


    override fun close() {
        resultSet?.close()
        super.close()
    }

    companion object {
        private fun getResultSetValue(resultSet: ResultSet, columnName: String): Any? {
            return try { resultSet.getObject(columnName) } catch (_: SQLException) { null }
        }
    }
}