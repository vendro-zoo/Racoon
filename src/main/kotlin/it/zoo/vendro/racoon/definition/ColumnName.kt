package it.zoo.vendro.racoon.definition

import it.zoo.vendro.racoon.configuration.RacoonConfiguration
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation

/**
 * An annotation that can be used on a primary constructor parameter or property to override
 * the default name of the column as returned by [it.zoo.vendro.racoon.habitat.configuration.RacoonConfiguration.Naming.columnNameMapper].
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class ColumnName(val name: String) {
    companion object {
        /**
         * Returns the column name for the given [KProperty1], first checking for an annotation,
         * then by applying the default mapper.
         *
         * @param field the property to get the column name for
         * @return the column name for the given property
         */
        fun getName(field: KProperty1<*, *>, config: RacoonConfiguration) =
            field.findAnnotation<ColumnName>()?.name ?: config.naming.columnNameMapper(field.name)

        /**
         * Returns the column name for the given [KParameter], first checking for an annotation,
         * then by applying the default mapper.
         *
         * @param field the property to get the column name for
         * @return the column name for the given property
         */
        fun getName(field: KParameter, config: RacoonConfiguration) =
            field.findAnnotation<ColumnName>()?.name ?: config.naming.columnNameMapper(field.name!!)
    }
}