package habitat

import commons.configuration.RacoonConfiguration
import commons.declarations.ParameterCaster
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

@Suppress("unused")
class Racoon(
    private val statement: Statement,
    private val originalQuery: String,
    private var processedQuery: String? = null,
    private var resultSet: ResultSet? = null,
    private val tableAliases: MutableMap<KClass<*>, String> = mutableMapOf(),
    private val namedParameters: MutableMap<String, String> = mutableMapOf(),
    private val indexedParameters: MutableMap<Int, String> = mutableMapOf(),
) : AutoCloseable {
    /**
     * Adds a table alias to be used when mapping the result of the query to a class.
     *
     * If the query contains the same table multiple times,
     * the alias must be re-set before each mapping.
     * @param klass The class to alias.
     * @param alias The alias to use.
     */
    fun setAlias(klass: KClass<*>, alias: String) = apply {
        tableAliases[klass] = alias
    }

    private fun replaceIndexed(query: String): String {
        val simpleRegex = Regex("=\\s*(\\?)")
        val surroundedRegex = Regex("\\(((\\?\\s*,?\\s*)+)\\)")

        val simpleMatch = simpleRegex.findAll(query)
        val surroundedMatch = surroundedRegex.findAll(query)

        val indexes = simpleMatch.toList().map {  it.groups[1]!!.range.first }.toMutableList()
        indexes.addAll(surroundedMatch.toList().map {
            Regex("\\?").findAll(it.groupValues[1])
                .toList().map { it2 -> it.groups[1]!!.range.first + it2.groups[0]!!.range.first }
        }.fold(mutableListOf()) { acc, it -> acc.addAll(it); acc })
        indexes.sort()

        return indexes.withIndex().fold(Pair(query, 0)) { a, it ->
            val v = it.value
            val i = it.index
            val replacing = indexedParameters[i + 1] ?: throw IllegalArgumentException("Indexed parameter `$i` not found")
            Pair(a.first.replaceRange((v+a.second)..(v+a.second), replacing), a.second+(replacing.length - 1))
        }.first
    }

    private fun replaceNamed(query: String): String {
        var result = query

        for ((key, value) in namedParameters) {
            val escapedKey = Regex.escape(key)
            var regex = Regex("=\\s*(:$escapedKey)")
            var match = regex.find(query)
            if (match == null) {
                regex = Regex("\\((?>\\s*:[:\\w\\d_]+\\s*,)*\\s*(:$escapedKey)\\s*(?>,\\s*:[:\\w\\d_]+\\s*)*\\)")
                match = regex.find(query)
                if (match == null) throw IllegalArgumentException("Could not find parameter $key in query `$query`")
            }

            result = result.replaceRange(match.groups[1]!!.range, value)
        }

        return result
    }

    fun calculateProcessedQuery() {
        if (processedQuery != null) return  // already calculated

        val indexReplaced = replaceIndexed(originalQuery)
        val namedReplaced = replaceNamed(indexReplaced)

        processedQuery = namedReplaced
    }

    /**
     * Executes the query and save the result in the [Racoon].
     * @return the [Racoon] itself
     */
    fun execute() = apply {
        resultSet = statement.executeQuery(processedQuery)
    }

    inline fun <reified T : Any> mapToClass(): List<T> = mapToClass(T::class)

    /**
     * Maps the result of the query to a class.
     * @param T The class to map to.
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
        val sqlAlias = tableAliases[tClass] ?: RacoonConfiguration.defaultTableAliasMapper(clazzName)

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

    fun <T: Any> setParam(index: Int, value: T): Racoon = apply {
        RacoonConfiguration.parameterCasters[value::class]?.let {
            @Suppress("UNCHECKED_CAST")
            indexedParameters[index] = (it as ParameterCaster<Any>).cast(value)
        } ?: throw NoSuchMethodException("A ParameterCaster for the class '${value::class.simpleName}' has not been registered")
    }

    fun <T: Any> setParam(name: String, value: T): Racoon = apply {
        RacoonConfiguration.parameterCasters[value::class]?.let {
            @Suppress("UNCHECKED_CAST")
            namedParameters[name] = (it as ParameterCaster<Any>).cast(value)
        } ?: throw NoSuchMethodException("A ParameterCaster for the class '${value::class.simpleName}' has not been registered")
    }

    // Debugging purposes
    // Example of retrieving the caster from the class
    fun getImplementationResult(value: Any): String {
        RacoonConfiguration.parameterCasters[value::class]?.let {
            @Suppress("UNCHECKED_CAST")
            return (it as ParameterCaster<Any>).cast(value)
        }
        return ""
    }

    /**
     * Closes the [Racoon] instance.
     * This method should be called when the [Racoon] instance is no longer needed.
     */
    override fun close() {
        resultSet?.close()
        statement.close()
    }
}