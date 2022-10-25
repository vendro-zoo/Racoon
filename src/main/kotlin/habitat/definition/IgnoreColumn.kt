package habitat.definition

import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class IgnoreColumn(val ignoreTarget: IgnoreTarget = IgnoreTarget.ALL) {
    companion object {
        fun shouldIgnore(field: KProperty1<*, *>, ignoreTarget: IgnoreTarget) =
            field.findAnnotation<IgnoreColumn>()?.ignoreTarget?.getList()?.contains(ignoreTarget) ?: false
    }
}
