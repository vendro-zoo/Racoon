package internals.expansions

import kotlin.reflect.KClass
import kotlin.reflect.KParameter

/**
 * Returns the [KClass] of the [KParameter]'s type.
 *
 * @return the [KClass] of the [KParameter]'s type.
 */
fun KParameter.asKClass() =
    type.asKClass()

/**
 * Returns the runtime type generic of the [KParameter].
 *
 * @param genericIndex The index of the generic type. If no value is provided, the first generic type is returned.
 * @return The generic type.
 */
fun KParameter.getRuntimeGeneric(genericIndex: Int = 0) =
    type.getRuntimeGeneric(genericIndex)

/**
 * Returns whether the [KParameter] can be null.
 *
 * @return True if the [KParameter] can be null, false otherwise.
 */
fun KParameter.isMarkedNullable() =
    this.type.isMarkedNullable

/**
 * Returns whether the [KParameter] is optional or nullable.
 *
 * @return True if the [KParameter] is optional or nullable, false otherwise.
 */
fun KParameter.isNullOrOptional() =
    this.isMarkedNullable() || this.isOptional