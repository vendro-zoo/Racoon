package habitat

import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

@Suppress("unused")
class Racoon(
    private val statement: Statement,
    private val query: String,
    var resultSet: ResultSet? = null,
    val tableAliases: MutableMap<KClass<*>, String> = mutableMapOf()
) : AutoCloseable{
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

    /**
     * Executes the query and save the result in the [Racoon].
     * @return the [Racoon] itself
     */
    fun execute() = apply {
        resultSet = statement.executeQuery(query)
    }

    /**
     * Maps the result of the query to a class.
     * @param T The class to map to.
     * @return A list of [T] containing the result of the mapping.
     * @throws ClassCastException If an error occurs during the mapping.
     * See the message of the exception for more details.
     */
    inline fun <reified T: Any> mapToClass(): List<T> {
        // If the query has not been executed yet, execute it
        resultSet ?: execute()
        val immutableResultSet = resultSet!!

        // Get the class of the type we want to map to
        val clazz = T::class
        val clazzName = clazz.simpleName ?: throw ClassCastException("Class name is null")

        // Get the table alias for the class or generate one if it isn't specified
        val sqlAlias = tableAliases[clazz] ?: clazzName.filter { x -> x.isUpperCase() }.lowercase()

        // Get the primary constructor of the class and its parameters
        val constructor = clazz.primaryConstructor ?: throw ClassCastException("$clazzName has no primary constructor")
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
     * Closes the [Racoon] instance.
     * This method should be called when the [Racoon] instance is no longer needed.
     */
    override fun close() {
        resultSet?.close()
        statement.close()
    }
}