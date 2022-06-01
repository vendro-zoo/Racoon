package commons.expansions

import kotlin.reflect.KClass
import kotlin.reflect.KParameter

fun KParameter.asKClass() =
    this.type.classifier as KClass<*>

fun KParameter.getRuntimeGeneric(genericIndex: Int = 0) =
    this.type.arguments[genericIndex].type!!.classifier as KClass<*>

fun KParameter.isMarkedNullable() =
    this.type.isMarkedNullable

fun KParameter.isNullOrOptional() =
    this.isMarkedNullable() || this.isOptional