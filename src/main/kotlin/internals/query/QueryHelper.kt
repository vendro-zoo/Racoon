package internals.query

import habitat.configuration.RacoonConfiguration
import habitat.definition.ColumnName
import habitat.definition.IgnoreColumn
import habitat.definition.IgnoreTarget
import habitat.definition.TableName
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

internal fun toValueForQuery(kProperty1: KProperty1<*, *>): String {
    val kClass = kProperty1.returnType.classifier as KClass<*>
    val caster = RacoonConfiguration.Casting.getFirstCaster(kClass)

    return if (caster != null) "${caster.toQueryPrefix}:${ColumnName.getName(kProperty1)}${caster.toQueryPostfix}"
    else ":${ColumnName.getName(kProperty1)}"
}

internal fun fromValueForQuery(kProperty1: KProperty1<*, *>, _alias: String = ""): String {
    val kClass = kProperty1.returnType.classifier as KClass<*>
    val caster = RacoonConfiguration.Casting.getFirstCaster(kClass)

    val alias = if (_alias.isEmpty()) "" else "`$_alias`."
    val asAlias = if (_alias.isEmpty()) "" else "${_alias}_"

    return if (caster != null && (caster.fromQueryPostfix.isNotBlank() || caster.fromQueryPrefix.isNotBlank()))
        "${caster.fromQueryPrefix}$alias`${ColumnName.getName(kProperty1)}`${caster.fromQueryPostfix} " +
            "as `$asAlias${ColumnName.getName(kProperty1)}`"
    else "$alias`${ColumnName.getName(kProperty1)}`"
}

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
    val properties = clazz.memberProperties.filter { mp ->
        IgnoreColumn.shouldIgnore(mp, IgnoreTarget.INSERT)
    }

    return "INSERT INTO `${TableName.getName(clazz)}` " +
            "(${properties.joinToString(separator = ",") { "`${ColumnName.getName(it)}`" }}) " +
            "VALUE (${properties.joinToString(separator = ",") { toValueForQuery(it) }})"
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
    val properties = clazz.memberProperties.filter { it.name != "id" }.filter { mp ->
        IgnoreColumn.shouldIgnore(mp, IgnoreTarget.UPDATE)
    }

    return "UPDATE `${TableName.getName(clazz)}` " +
            "SET ${properties.joinToString(separator = ",") { "`${ColumnName.getName(it)}`=${toValueForQuery(it)}" }} " +
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
fun <T: Any> generateSelectQueryK(clazz: KClass<T>): String {
    return "SELECT ${generateSelectColumnsK(clazz)} " +
            "FROM `${TableName.getName(clazz)}` WHERE `id`=:id"
}

inline fun <reified T: Any> generateSelectColumns(alias: String = "") = generateSelectColumnsK(T::class, alias)

fun <T: Any> generateSelectColumnsK(clazz: KClass<T>, alias: String = "") =
    clazz.memberProperties.filter { mp ->
        IgnoreColumn.shouldIgnore(mp, IgnoreTarget.SELECT)
    }.joinToString(separator = ",") { fromValueForQuery(it, alias) }

/**
 * Creates a delete query for the given class.
 * The query returned is a delete query where the only filter is the id of the object. The value of the id can be specified with the named parameter "id".
 *
 * @param clazz The class to create the delete query for.
 * @return A string containing the delete query for the given class.
 */
fun <T: Any> generateDeleteQueryK(clazz: KClass<T>): String =
    "DELETE FROM `${TableName.getName(clazz)}` WHERE `id`=:id"