package internals.query

import habitat.definition.ColumnName
import habitat.definition.TableName
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

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

    return "INSERT INTO `${TableName.getName(clazz)}` " +
            "(${properties.joinToString(separator = ",") { "`${ColumnName.getName(it)}`" }}) " +
            "VALUE (${properties.joinToString(",") { ":${ColumnName.getName(it)}" }})"
}

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

    return "UPDATE `${TableName.getName(clazz)}` " +
            "SET ${properties.joinToString(separator = ",") { "`${ColumnName.getName(it)}`=:${ColumnName.getName(it)}" }} " +
            "WHERE `id`=:id"
}

/**
 * Creates a select query for the given class.
 *
 * The query returned is a select query where the only filter is the id of the object.
 * The value of the id can be specified with the named parameter "id".
 *
 * @param clazz The class to create the select query for.
 * @return A string containing the select query for the given class.
 */
fun <T: Any> generateSelectQueryK(clazz: KClass<T>): String =
    "SELECT * FROM `${TableName.getName(clazz)}` WHERE `id`=:id"

/**
 * Creates a delete query for the given class.
 * The query returned is a delete query where the only filter is the id of the object. The value of the id can be specified with the named parameter "id".
 *
 * @param clazz The class to create the delete query for.
 * @return A string containing the delete query for the given class.
 */
fun <T: Any> generateDeleteQueryK(clazz: KClass<T>): String =
    "DELETE FROM `${TableName.getName(clazz)}` WHERE `id`=:id"