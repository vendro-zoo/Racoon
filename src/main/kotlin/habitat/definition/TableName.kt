package habitat.definition

import habitat.configuration.RacoonConfiguration
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

/**
 * An annotation that can be used on a class implementing the [Table] interface to override
 * the default name of the table as returned by [habitat.configuration.RacoonConfiguration.Naming.tableNameMapper]
 * and [habitat.configuration.RacoonConfiguration.Naming.tableAliasMapper].
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class TableName(val name: String, val alias: String = "") {
    companion object {
        /**
         * Returns the table name for the given [KClass], first checking for an annotation,
         * then by applying the default mapper.
         *
         * @param kClass the class to get the table name for
         * @return the table name for the given class
         */
        fun getName(kClass: KClass<*>): String =
            kClass.findAnnotation<TableName>()?.name ?:
            RacoonConfiguration.Naming.tableNameMapper(kClass.simpleName ?:
            throw IllegalArgumentException("The class does not have a name"))

        /**
         * Returns the table alias for the given [KClass], first checking for an annotation,
         * then by applying the default mapper.
         *
         * @param kClass the class to get the table alias for
         * @return the table alias for the given class
         */
        fun getAlias(kClass: KClass<*>): String {
            val alias = kClass.findAnnotation<TableName>()?.alias
            return if (alias.isNullOrEmpty())
                RacoonConfiguration.Naming.tableAliasMapper(kClass.simpleName ?:
                throw IllegalArgumentException("The class does not have a name"))
            else
                alias
        }
    }
}