package commons.query

import habitat.configuration.RacoonConfiguration
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

const val NULL_CLASS_NAME = "Class name cannot be null"

/**
 * Behaves like [generateInsertQueryK], but instead of passing the class as a normal parameter, it is passed as a reified type.
 *
 * @see generateInsertQueryK
 */
inline fun <reified T: Any> generateInsertQuery() = generateInsertQueryK(T::class)

/**
 * Creates an insert query for the given class.
 *
 * All the properties of the class are used to create the insert query.
 * The values of the query are marked with named parameters.
 *
 * @param clazz The class to create the insert query for.
 * @return A string containing the insert query for the given class.
 * @throws IllegalArgumentException If the class name is null.
 */
fun <T: Any> generateInsertQueryK(clazz: KClass<T>): String {
    val properties = clazz.memberProperties

    val className = clazz.simpleName ?: throw IllegalArgumentException(NULL_CLASS_NAME)

    return "INSERT INTO `${RacoonConfiguration.Naming.getName(className)}` " +
            "(${properties.joinToString(separator = ",") { "`${it.name}`" }}) " +
            "VALUE (${properties.joinToString(",") { ":${it.name}" }})"
}

/**
 * Behaves like [generateUpdateQueryK], but instead of passing the class as a normal parameter, it is passed as a reified type.
 *
 * @see generateUpdateQueryK
 */
inline fun <reified T: Any> generateUpdateQuery() = generateUpdateQueryK(T::class)

/**
 * Creates an update query for the given class.
 *
 * All the properties of the class are used to create the update query. The values of the query are marked with named parameters.
 * The only exception is the property with the name "id", which is used to identify the object to update.
 *
 * @param clazz The class to create the update query for.
 * @return A string containing the update query for the given class.
 */
fun <T: Any> generateUpdateQueryK(clazz: KClass<T>): String {
    val properties = clazz.memberProperties.filter { it.name != "id" }

    val className = clazz.simpleName ?: throw IllegalArgumentException(NULL_CLASS_NAME)

    return "UPDATE `${RacoonConfiguration.Naming.getName(className)}` " +
            "SET ${properties.joinToString(separator = ",") { "`${it.name}`=:${it.name}" }} " +
            "WHERE `id`=:id"
}

/**
 * Behaves like [generateSelectQueryK], but instead of passing the class as a normal parameter, it is passed as a reified type.
 *
 * @see generateSelectQueryK
 */
inline fun <reified T: Any> generateSelectQuery() = generateSelectQueryK(T::class)

/**
 * Creates a select query for the given class.
 *
 * The query returned is a select query where the only filter is the id of the object.
 * The value of the id can be specified with the named parameter "id".
 *
 * @param clazz The class to create the select query for.
 * @return A string containing the select query for the given class.
 */
fun <T: Any> generateSelectQueryK(clazz: KClass<T>): String {
    val className = clazz.simpleName ?: throw IllegalArgumentException(NULL_CLASS_NAME)

    return "SELECT * FROM `${RacoonConfiguration.Naming.getName(className)}` WHERE `id`=:id"
}


/**
 * Behaves like [generateDeleteQueryK], but instead of passing the class as a normal parameter, it is passed as a reified type.
 *
 * @see generateDeleteQueryK
 */
inline fun <reified T: Any> generateDeleteQuery() = generateDeleteQueryK(T::class)

/**
 * Creates a delete query for the given class.
 * The query returned is a delete query where the only filter is the id of the object. The value of the id can be specified with the named parameter "id".
 *
 * @param clazz The class to create the delete query for.
 * @return A string containing the delete query for the given class.
 */
fun <T: Any> generateDeleteQueryK(clazz: KClass<T>): String {
    val className = clazz.simpleName ?: throw IllegalArgumentException(NULL_CLASS_NAME)

    return "DELETE FROM `${RacoonConfiguration.Naming.getName(className)}` WHERE `id`=:id"
}