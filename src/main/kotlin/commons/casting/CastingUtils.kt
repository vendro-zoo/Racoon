package commons.casting

import java.sql.ResultSet
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.superclasses

/**
 * Casts a parameter to a considered equivalent type.
 *
 * This method is used in order to prevent small type mismatches, like casting a [Long] to a [Int], and vice versa.
 *
 * @param param The parameter of the class' constructor to cast.
 * @param value The value retrieved from the [ResultSet]
 */
internal fun castEquivalent(param: KParameter, value: Any): Any {
    // Getting the classes of the parameters
    val vClass: KClass<out Any> = value::class
    val pClass: KClass<out Any> = param.type.classifier as KClass<out Any>

    // Getting the super classes of the parameters
    val vSuper: KClass<out Any>? = vClass.superclasses.firstOrNull()
    val pSuper: KClass<out Any>? = pClass.superclasses.firstOrNull()

    // If the classes are the same, we can return the value
    if (vClass == pClass) return value

    // If are both numbers, we can cast the value
    if (vSuper != null && pSuper != null && vSuper == pSuper && vSuper == Number::class) {
        return when (pClass) {
            Int::class -> (value as Number).toInt()
            Long::class -> (value as Number).toLong()
            Float::class -> (value as Number).toFloat()
            Double::class -> (value as Number).toDouble()
            Short::class -> (value as Number).toShort()
            Byte::class -> (value as Number).toByte()
            else -> value
        }
    }

    throw IllegalArgumentException("Cannot cast $value from ${vClass.simpleName} to ${pClass.simpleName}")
}