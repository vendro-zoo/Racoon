package habitat.racoons

import commons.casting.castEquivalent
import commons.expansions.asKClass
import commons.expansions.asMapEntry
import commons.expansions.getRuntimeGeneric
import commons.query.QueryProcessing
import habitat.RacoonManager
import habitat.configuration.RacoonConfiguration
import habitat.context.ParameterCasterContext
import habitat.definition.LazyId
import habitat.definition.Table
import habitat.definition.TableName
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

    // TODO: Add flag can be null
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
        val list = mutableListOf<T>()

        // Reset the result set pointer to the beginning
        immutableResultSet.beforeFirst()

        // Loop through the result set
        while (immutableResultSet.next()) {
            // Create a map of the column names to their values
            val map: Map<KParameter, Any?> = parameters.associateWith {
                // Get the name of the parameter
                val name = it.name ?: throw ClassCastException("Can't access a property because it's name is null")

                val value = getResultSetValue(immutableResultSet, "$sqlAlias.$name") ?:
                    getResultSetValue(immutableResultSet, name)

                if (value == null && !it.isOptional && it.asKClass() != LazyId::class)
                    throw ClassCastException("resultSet has no column '$sqlAlias.$name' or '$name'")

                return@associateWith value
            }.map {
                @Suppress("UNCHECKED_CAST")
                if (it.value == null && it.key.asKClass() == LazyId::class)
                    return@map (it.key to LazyId.empty(it.key.getRuntimeGeneric() as KClass<Table>)).asMapEntry()
                it
            }.filter { it.value != null }.map {
                if (it.value is LazyId<*>) return@map it.toPair()

                // Getting the user defined type [ParameterCaster], if it exists
                var kClassifier = it.key.asKClass()
                val caster = RacoonConfiguration.Casting.getCaster(kClassifier)

                @Suppress("UNCHECKED_CAST")
                if (kClassifier == LazyId::class) kClassifier =
                    it.key.getRuntimeGeneric() as KClass<Table>

                // Casting with the user defined type [ParameterCaster], otherwise casting with the internal caster
                val value = caster?.uncast(it.value!!, ParameterCasterContext(manager, kClassifier))
                    ?: castEquivalent(it.key, it.value!!)

                it.key to value
            }.toMap()

            // Create a new instance of the class and add it to the list
            list.add(constructor.callBy(map))
        }

        // Return the list
        return list.toList()
    }

    // TODO: Add flag can be null
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