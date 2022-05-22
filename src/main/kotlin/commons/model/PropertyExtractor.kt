package commons.model

import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

/**
 * Behaves like [getValueK], but instead of passing the class as a normal parameter, it is passed as a reified type.
 *
 * @param T The type that is being inserted.
 * @param obj The object to insert.
 * @param propertyName The name of the property containing the value.
 * @return The value of the property.
 */
inline fun <reified T : Any> getValue(obj: T, propertyName: String): Any? = getValueK(obj, propertyName, T::class)

/**
 * Gets the value of a property of an object.
 *
 * @param obj The object containing the value to extract.
 * @param propertyName The name of the property to extract.
 * @param kClass The class of the object.
 * @return The value of the property.
 */
fun <T : Any> getValueK(obj: T, propertyName: String, kClass: KClass<T>): Any? {
    val field = kClass.memberProperties.find { it.name == propertyName }
    return field?.get(obj)
}