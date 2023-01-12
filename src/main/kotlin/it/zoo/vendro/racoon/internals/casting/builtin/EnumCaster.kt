package it.zoo.vendro.racoon.internals.casting.builtin

import it.zoo.vendro.racoon.habitat.context.FromParameterCasterContext
import it.zoo.vendro.racoon.habitat.context.ToParameterCasterContext
import it.zoo.vendro.racoon.habitat.definition.ColumnName
import it.zoo.vendro.racoon.internals.casting.ParameterCaster
import it.zoo.vendro.racoon.internals.extensions.asKClass
import kotlin.reflect.KClass

class EnumCaster : ParameterCaster<Enum<*>, String> {
    override fun toQuery(parameter: Enum<*>, context: ToParameterCasterContext): String =
        getNameFromEnum(parameter)

    override fun fromQuery(parameter: String, context: FromParameterCasterContext): Enum<*> =
        getEnumFromName(parameter, context.actualType.asKClass())

    private companion object {
        inline fun <reified T: Annotation> Array<Annotation>.findAnnotation(): T? =
            find { it is T }?.let { it as T }

        fun getNameFromEnum(enum: Enum<*>): String =
            enum.declaringClass.fields
                // Find the field with the same name as the enum value
                .find { it.name == enum.name }?.let {
                    // Find the ColumnName annotation on the field or return the field name
                    it.annotations.findAnnotation<ColumnName>()?.name ?: it.name
                }!!

        fun getEnumFromName(name: String, kClass: KClass<*>) =
            @Suppress("UPPER_BOUND_VIOLATED", "UNCHECKED_CAST", "RemoveExplicitTypeArguments")
            java.lang.Enum.valueOf<Any>(kClass.java as Class<Any>, getEnumNameFromName(name, kClass)) as Enum<*>

        fun getEnumNameFromName(name: String, kClass: KClass<*>) =
            kClass.java.fields
                .find { field ->
                    field.annotations.findAnnotation<ColumnName>()?.
                    let { it.name == name } ?: false || field.name == name
                }?.name ?: throw IllegalArgumentException("No enum constant found for name $name")
    }
}