package internals.expansions

import kotlin.reflect.KClass
import kotlin.reflect.KType

fun KType.asKClass() = classifier as KClass<*>

fun KType.getRuntimeGeneric(genericIndex: Int = 0) =
    arguments[genericIndex].type?.asKClass()