package habitat.definition

import habitat.configuration.RacoonConfiguration
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class ColumnName(val name: String) {
    companion object {
        fun getName(field: KProperty1<*, *>) =
            field.findAnnotation<ColumnName>()?.name ?: RacoonConfiguration.Naming.getColumnName(field.name)
    }
}