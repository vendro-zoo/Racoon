package internals.casting

import internals.extensions.asKClass
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.superclasses

/**
 * Behaves like [castEquivalentK] but receives a [KParameter] instead of a [KClass].
 *
 * @see castEquivalentK
 */
internal fun castEquivalent(kParameter: KParameter, value: Any): Any =
    castEquivalentK(kParameter.asKClass(), value)

/**
 * Casts a parameter to a considered equivalent type.
 *
 * This method is used in order to prevent small type mismatches, like casting a [Long] to a [Int], and vice versa.
 *
 * @param kClass The class to cast to.
 * @param value The value retrieved from the [ResultSet]
 */
internal fun castEquivalentK(kClass: KClass<out Any>, value: Any): Any {
    // Getting the class of the value
    val vClass: KClass<out Any> = value::class

    // Getting the super classes of the parameters
    val vSuper: KClass<out Any>? = vClass.superclasses.firstOrNull()
    val pSuper: KClass<out Any>? = kClass.superclasses.firstOrNull()

    // If the classes are the same, we can return the value
    if (vClass == kClass) return value

    // Timestamp conversions
    if (vClass == Timestamp::class && kClass == Long::class) return (value as Timestamp).time
    if (vClass == Timestamp::class && kClass == Date::class) return Date((value as Timestamp).time)

    // Get custom unsigned types (if possible)
    val unsignedValue = getUType(value)
    val unsignedPClass = getUType(kClass)

    if (unsignedValue != null) {
        if (unsignedPClass != null) {
            // If both are unsigned, convert to other standard unsigned type
            return unsignedValue.toUType(unsignedPClass)
        }

        // If the parameter is not unsigned but is a number, convert the value
        @Suppress("UNCHECKED_CAST")
        if (pSuper == Number::class)
            return unsignedValue.toSType(kClass as KClass<out Number>)
    } else if (unsignedPClass != null && vSuper == Number::class) {
        // If the value is not unsigned but is a number, convert the value
        return castToUnsigned(value as Number, unsignedPClass)
    }

    // If are both numbers, we can cast the value
    if (vSuper != null && pSuper != null && vSuper == pSuper && vSuper == Number::class) {
        @Suppress("UNCHECKED_CAST")
        return castSigned(value as Number, kClass as KClass<out Number>)
    }

    throw IllegalArgumentException("Cannot cast $value from ${vClass.simpleName} to ${kClass.simpleName}")
}

private fun castSigned(value: Number, pClass: KClass<out Number>) = when (pClass) {
    Int::class -> value.toInt()
    Long::class -> value.toLong()
    Float::class -> value.toFloat()
    Double::class -> value.toDouble()
    Short::class -> value.toShort()
    Byte::class -> value.toByte()
    else -> value
}

private fun castToUnsigned(value: Number, uType: KClass<out UType>) = when(uType) {
    CUByte::class -> value.toByte().toUByte()
    CUShort::class -> value.toShort().toUShort()
    CUInt::class -> value.toInt().toUInt()
    CULong::class -> value.toLong().toULong()
    else -> throw IllegalArgumentException("Cannot cast $value to ${uType.simpleName}")
}

private fun getUType(value: Any) = when (value::class) {
    UInt::class -> CUInt(value as UInt)
    ULong::class -> CULong(value as ULong)
    UByte::class -> CUByte(value as UByte)
    UShort::class -> CUShort(value as UShort)
    else -> null
}

private fun getUType(type: KClass<out Any>) = when (type) {
    UInt::class -> CUInt::class
    ULong::class -> CULong::class
    UByte::class -> CUByte::class
    UShort::class -> CUShort::class
    else -> null
}

private interface UType {
    fun toUInt(): UInt
    fun toULong(): ULong
    fun toUShort(): UShort
    fun toUByte(): UByte

    fun toUType(uType: KClass<out UType>) = when (uType) {
        CUInt::class -> toUInt()
        CULong::class -> toULong()
        CUShort::class -> toUShort()
        CUByte::class -> toUByte()
        else -> throw IllegalArgumentException("Cannot cast $this to $uType")
    }

    fun toSType(sType: KClass<out Number>) = when (sType) {
        Int::class -> toUInt().toInt()
        Long::class -> toULong().toLong()
        Short::class -> toUShort().toShort()
        Byte::class -> toUByte().toByte()
        else -> throw IllegalArgumentException("Cannot cast $this to $sType")
    }
}

private class CUInt(val value: UInt) : UType {
    override fun toUInt(): UInt = value
    override fun toULong(): ULong = value.toULong()
    override fun toUShort(): UShort = value.toUShort()
    override fun toUByte(): UByte = value.toUByte()
}

private class CULong(val value: ULong) : UType {
    override fun toUInt(): UInt = value.toUInt()
    override fun toULong(): ULong = value
    override fun toUShort(): UShort = value.toUShort()
    override fun toUByte(): UByte = value.toUByte()
}

private class CUShort(val value: UShort) : UType {
    override fun toUInt(): UInt = value.toUInt()
    override fun toULong(): ULong = value.toULong()
    override fun toUShort(): UShort = value
    override fun toUByte(): UByte = value.toUByte()
}

private class CUByte(val value: UByte) : UType {
    override fun toUInt(): UInt = value.toUInt()
    override fun toULong(): ULong = value.toULong()
    override fun toUShort(): UShort = value.toUShort()
    override fun toUByte(): UByte = value
}