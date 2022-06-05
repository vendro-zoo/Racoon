package internals.expansions

import kotlin.reflect.KClass
import kotlin.reflect.KParameter

fun KParameter.asKClass() =
    type.asKClass()

fun KParameter.getRuntimeGeneric(genericIndex: Int = 0) =
    this.type.arguments[genericIndex].type!!.classifier as KClass<*>

fun KParameter.isMarkedNullable() =
    this.type.isMarkedNullable

fun KParameter.isNullOrOptional() =
    this.isMarkedNullable() || this.isOptional