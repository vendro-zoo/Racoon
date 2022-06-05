package internals.extensions

import kotlin.reflect.KClass
import kotlin.reflect.KType


/**
 * Returns the [KClass] of the [KType].
 *
 * @return the [KClass] of the [KType].
 */
fun KType.asKClass() = classifier as KClass<*>

/**
 * Returns the runtime type generic of the [KType].
 *
 * @param genericIndex The index of the generic type. If no value is provided, the first generic type is returned.
 * @return The generic type.
 */
fun KType.getRuntimeGeneric(genericIndex: Int = 0) =
    arguments[genericIndex].type