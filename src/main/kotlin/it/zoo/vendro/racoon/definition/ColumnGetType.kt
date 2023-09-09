package it.zoo.vendro.racoon.definition

import java.sql.ResultSet
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
/**
 * Annotation used to specify how to get the value of a column from a [ResultSet].
 * By default, the value of the column is retrieved using [ColumnGetTypes.Object].
 */
annotation class ColumnGetType(val method: ColumnGetTypes) {
    companion object {
        fun getExtractionMethod(parameter: KParameter) =
            parameter.findAnnotation<ColumnGetType>()?.method?.extractor
                ?: ColumnExtraction.Object
    }
}
