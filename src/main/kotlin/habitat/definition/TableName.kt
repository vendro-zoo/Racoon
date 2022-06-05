package habitat.definition

import habitat.configuration.RacoonConfiguration
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class TableName(val name: String, val alias: String = "") {
    companion object {
        fun getName(kClass: KClass<*>): String =
            kClass.findAnnotation<TableName>()?.name ?:
            RacoonConfiguration.Naming.tableNameMapper(kClass.simpleName ?:
            throw IllegalArgumentException("The class does not have a name"))

        fun getAlias(kClass: KClass<*>): String =
            kClass.findAnnotation<TableName>()?.alias ?:
            RacoonConfiguration.Naming.tableAliasMapper(kClass.simpleName ?:
            throw IllegalArgumentException("The class does not have a name"))

    }
}