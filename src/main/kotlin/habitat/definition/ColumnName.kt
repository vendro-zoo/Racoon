package habitat.definition

import habitat.configuration.RacoonConfiguration
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class ColumnName(val name: String)

fun <T, V> getColumnName(field: KProperty1<T, V>): String =
    field.findAnnotation<ColumnName>()?.name ?: RacoonConfiguration.Naming.getColumnName(field.name)