package commons.model

import habitat.configuration.RacoonConfiguration
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

const val NULL_CLASS_NAME = "Class name cannot be null"

inline fun <reified T: Any> generateInsertQuery() = generateInsertQuery(T::class)

fun <T: Any> generateInsertQuery(clazz: KClass<T>): String {
    val properties = clazz.memberProperties

    val className = clazz.simpleName ?: throw IllegalArgumentException(NULL_CLASS_NAME)

    return "INSERT INTO `${RacoonConfiguration.Naming.getName(className)}` " +
            "(${properties.joinToString(separator = ",") { "`${it.name}`" }}) " +
            "VALUE (${properties.joinToString(",") { ":${it.name}" }})"
}

inline fun <reified T: Any> generateUpdateQuery() = generateUpdateQuery(T::class)

fun <T: Any> generateUpdateQuery(clazz: KClass<T>): String {
    val properties = clazz.memberProperties.filter { it.name != "id" }

    val className = clazz.simpleName ?: throw IllegalArgumentException(NULL_CLASS_NAME)

    return "UPDATE `${RacoonConfiguration.Naming.getName(className)}` " +
            "SET ${properties.joinToString(separator = ",") { "`${it.name}` = :${it.name}" }} " +
            "WHERE id = :id"
}

inline fun <reified T: Any> generateSelectQuery() = generateSelectQuery(T::class)

fun <T: Any> generateSelectQuery(clazz: KClass<T>): String {
    val className = clazz.simpleName ?: throw IllegalArgumentException(NULL_CLASS_NAME)

    return "SELECT * FROM `${RacoonConfiguration.Naming.getName(className)}` where id = :id"
}