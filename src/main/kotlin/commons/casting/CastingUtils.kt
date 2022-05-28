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

    val unsignedValue = getUType(value)
    val unsignedPClass = getUType(pClass)

    if (unsignedValue != null) {
        if (unsignedPClass != null) {
            return unsignedValue.toUType(unsignedPClass)
        }
        @Suppress("UNCHECKED_CAST")
        return unsignedValue.toSType(pClass as KClass<out Number>)
    } else if (unsignedPClass != null) {
        return castToUnsigned(value as Number, unsignedPClass)
    }

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