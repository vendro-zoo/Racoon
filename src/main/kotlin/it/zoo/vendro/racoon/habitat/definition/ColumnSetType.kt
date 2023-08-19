package it.zoo.vendro.racoon.habitat.definition

import java.sql.PreparedStatement
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
/**
 * Annotation used to specify how to set the value of a column into a [PreparedStatement].
 * By default, the value of the column is set using [ColumnSetTypes.Object].
 */
annotation class ColumnSetType(val method: ColumnSetTypes) {
    companion object {
        fun getInsertionMethod(parameter: KProperty<*>): ColumnInsertion =
            parameter.findAnnotation<ColumnSetType>()?.method?.inserter
                ?: ColumnInsertion.Object
    }
}
