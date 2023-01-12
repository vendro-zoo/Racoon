package it.zoo.vendro.racoon.habitat.definition

import java.sql.ResultSet
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
/**
 * Annotation used to specify how to get the value of a column from a [ResultSet].
 * By default, the value of the column is retrieved using [ExtractionMethodType.Object].
 */
annotation class ColumnExtractionMethod(val method: ExtractionMethodType) {
    companion object {
        fun getExtractionMethod(parameter: KParameter) =
            parameter.findAnnotation<ColumnExtractionMethod>()?.method?.extractor
                ?: ExtractionMethod.Object
    }
}
