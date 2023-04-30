package it.zoo.vendro.racoon.internals.query

import it.zoo.vendro.racoon.habitat.configuration.RacoonConfiguration
import it.zoo.vendro.racoon.habitat.definition.ColumnName
import it.zoo.vendro.racoon.habitat.definition.IgnoreColumn
import it.zoo.vendro.racoon.habitat.definition.IgnoreTarget
import it.zoo.vendro.racoon.habitat.definition.TableName
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

internal fun toValueForQuery(kProperty1: KProperty1<*, *>, config: RacoonConfiguration): String {
    val kClass = kProperty1.returnType.classifier as KClass<*>
    val caster = config.casting.getFirstCaster(kClass)

    return if (caster != null) "${caster.toQueryPrefix}:${
        ColumnName.getName(
            kProperty1,
            config
        )
    }${caster.toQueryPostfix}"
    else ":${ColumnName.getName(kProperty1, config)}"
}

internal fun fromValueForQuery(kProperty1: KProperty1<*, *>, config: RacoonConfiguration, alias: String = ""): String {
    val kClass = kProperty1.returnType.classifier as KClass<*>
    val caster = config.casting.getFirstCaster(kClass)

    val q = config.connection.connectionSettings.protocol.quotation.identifierQuote

    val alias1 = if (alias.isEmpty()) "" else "$q$alias$q."
    val asAlias = if (alias.isEmpty()) "" else "${alias}_"

    return if (caster != null && (caster.fromQueryPostfix.isNotBlank() || caster.fromQueryPrefix.isNotBlank()))
        "${caster.fromQueryPrefix}$alias1$q${ColumnName.getName(kProperty1, config)}$q${caster.fromQueryPostfix} " +
                "as $q$asAlias${ColumnName.getName(kProperty1, config)}$q"
    else "$alias1$q${ColumnName.getName(kProperty1, config)}$q"
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
fun <T : Any> generateInsertQueryK(clazz: KClass<T>, config: RacoonConfiguration): String {
    val properties = clazz.memberProperties.filter { mp ->
        !IgnoreColumn.shouldIgnore(mp, IgnoreTarget.INSERT)
    }

    val q = config.connection.connectionSettings.protocol.quotation.identifierQuote

    return "INSERT INTO $q${TableName.getName(clazz, config)}$q " +
            "(${properties.joinToString(separator = ",") { "$q${ColumnName.getName(it, config)}$q" }}) " +
            "VALUES (${properties.joinToString(separator = ",") { toValueForQuery(it, config) }})"
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
fun <T : Any> generateUpdateQueryK(clazz: KClass<T>, config: RacoonConfiguration): String {
    val properties = clazz.memberProperties.filter { it.name != "id" }.filter { mp ->
        !IgnoreColumn.shouldIgnore(mp, IgnoreTarget.UPDATE)
    }

    val q = config.connection.connectionSettings.protocol.quotation.identifierQuote

    return "UPDATE $q${TableName.getName(clazz, config)}$q " +
            "SET ${
                properties.joinToString(separator = ",") {
                    "$q${ColumnName.getName(it, config)}$q=${toValueForQuery(it, config)}"
                }
            } WHERE ${q}id$q=:id"
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
fun <T : Any> generateSelectQueryK(clazz: KClass<T>, config: RacoonConfiguration): String {
    val q = config.connection.connectionSettings.protocol.quotation.identifierQuote

    return "SELECT ${generateSelectColumnsK(clazz, config)} " +
            "FROM $q${TableName.getName(clazz, config)}$q WHERE ${q}id$q=:id"
}

inline fun <reified T : Any> generateSelectColumns(config: RacoonConfiguration, alias: String = "") =
    generateSelectColumnsK(T::class, config, alias)

fun <T : Any> generateSelectColumnsK(clazz: KClass<T>, config: RacoonConfiguration, alias: String = "") =
    clazz.memberProperties.filter { mp ->
        !IgnoreColumn.shouldIgnore(mp, IgnoreTarget.SELECT)
    }.joinToString(separator = ",") { fromValueForQuery(it, config, alias) }

/**
 * Creates a delete query for the given class.
 * The query returned is a delete query where the only filter is the id of the object. The value of the id can be specified with the named parameter "id".
 *
 * @param clazz The class to create the delete query for.
 * @return A string containing the delete query for the given class.
 */
fun <T : Any> generateDeleteQueryK(clazz: KClass<T>, config: RacoonConfiguration): String {
    val q = config.connection.connectionSettings.protocol.quotation.identifierQuote

    return "DELETE FROM $q${TableName.getName(clazz, config)}$q WHERE ${q}id$q=:id"
}
