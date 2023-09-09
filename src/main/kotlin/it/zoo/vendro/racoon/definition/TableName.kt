package it.zoo.vendro.racoon.definition

import it.zoo.vendro.racoon.configuration.RacoonConfiguration
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

/**
 * An annotation that can be used on a class implementing the [Table] interface to override
 * the default name of the table as returned by [it.zoo.vendro.racoon.connection.configuration.RacoonConfiguration.Naming.tableNameMapper]
 * and [it.zoo.vendro.racoon.connection.configuration.RacoonConfiguration.Naming.tableAliasMapper].
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
        fun getName(kClass: KClass<*>, config: RacoonConfiguration): String =
            kClass.findAnnotation<TableName>()?.name ?: config.naming.tableNameMapper(
                kClass.simpleName ?: throw IllegalArgumentException("The class does not have a name")
            )

        /**
         * Returns the table alias for the given [KClass], first checking for an annotation,
         * then by applying the default mapper.
         *
         * @param kClass the class to get the table alias for
         * @return the table alias for the given class
         */
        fun getAlias(kClass: KClass<*>, config: RacoonConfiguration): String {
            val alias = kClass.findAnnotation<TableName>()?.alias
            return if (alias.isNullOrEmpty()) config.naming.tableAliasMapper(
                kClass.simpleName ?: throw IllegalArgumentException("The class does not have a name")
            ) else alias
        }
    }
}