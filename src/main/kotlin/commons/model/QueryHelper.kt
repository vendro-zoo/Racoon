package commons.model

import habitat.configuration.RacoonConfiguration
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

inline fun <reified T: Any> generateInsertQuery() = generateInsertQuery(T::class)

fun <T: Any> generateInsertQuery(clazz: KClass<T>): String {
    val properties = clazz.memberProperties

    val className = clazz.simpleName ?: throw IllegalArgumentException("Class name cannot be null")

    return "INSERT INTO `${RacoonConfiguration.Naming.getName(className)}` " +
            "(${properties.joinToString(separator = ",") { "`${it.name}`" }}) " +
            "VALUE (${properties.joinToString(",") { "?" }})"
}