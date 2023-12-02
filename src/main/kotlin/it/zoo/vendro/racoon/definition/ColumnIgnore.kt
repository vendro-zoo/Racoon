package it.zoo.vendro.racoon.definition

import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class ColumnIgnore(val ignoreTarget: IgnoreTarget = IgnoreTarget.ALL) {
    companion object {
        fun shouldIgnore(field: KProperty1<*, *>, ignoreTarget: IgnoreTarget) =
            field.findAnnotation<ColumnIgnore>()?.ignoreTarget?.getList()?.contains(ignoreTarget) ?: false
        fun shouldIgnore(field: KParameter, ignoreTarget: IgnoreTarget) =
            field.findAnnotation<ColumnIgnore>()?.ignoreTarget?.getList()?.contains(ignoreTarget) ?: false
    }
}
