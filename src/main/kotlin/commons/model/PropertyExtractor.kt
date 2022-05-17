package commons.model

import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

inline fun <reified T : Any> getValue(obj: T, propertyName: String): Any? = getValue(obj, propertyName, T::class)

fun <T : Any> getValue(obj: T, propertyName: String, tClass: KClass<T>): Any? {
    val field = tClass.memberProperties.find { it.name == propertyName }
    return field?.get(obj)
}