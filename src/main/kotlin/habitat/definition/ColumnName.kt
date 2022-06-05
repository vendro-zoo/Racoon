package habitat.definition

import habitat.configuration.RacoonConfiguration
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class ColumnName(val name: String) {
    companion object {
        fun getName(field: KProperty1<*, *>) =
            field.findAnnotation<ColumnName>()?.name ?: RacoonConfiguration.Naming.columnNameMapper(field.name)
        fun getName(field: KParameter) =
            field.findAnnotation<ColumnName>()?.name ?: RacoonConfiguration.Naming.columnNameMapper(field.name!!)
    }
}