package habitat.definition

import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class ColumnExtractionMethod(val method: ExtractionMethodType) {
    companion object {
        fun getExtractionMethod(parameter: KParameter) =
            parameter.findAnnotation<ColumnExtractionMethod>()?.method?.extractor
                ?: ExtractionMethod.Object
    }
}
