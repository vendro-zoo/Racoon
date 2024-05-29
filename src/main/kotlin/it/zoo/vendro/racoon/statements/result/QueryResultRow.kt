package it.zoo.vendro.racoon.statements.result

import it.zoo.vendro.racoon.context.FromQuerySerDeContext
import it.zoo.vendro.racoon.definition.*
import it.zoo.vendro.racoon.internals.extensions.asKClass
import it.zoo.vendro.racoon.internals.extensions.isMarkedNullable
import it.zoo.vendro.racoon.internals.extensions.isNullOrOptional
import it.zoo.vendro.racoon.serdes.castEquivalent
import java.math.BigDecimal
import java.sql.ResultSet
import java.sql.SQLException
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.createType
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.typeOf

class QueryResultRow(val queryResult: QueryResult) {
    internal val statement get() = queryResult.statement
    internal val manager get() = statement.manager
    val resultSet get() = queryResult.resultSet
    internal val config get() = manager.config

    /**
     * Maps the result of the query to a class.
     *
     * If the query has not been executed yet, it is executed first.
     *
     * An optional parameter `nullable` can be used to specify if the result of the mapping can contain `null` values.
     *
     * @param tClass The class to map to.
     * @param nullable If `true`, the result of the mapping can contain `null` values. The default value is `false`.
     * @return A list of [T] containing the result of the mapping.
     * @throws ClassCastException If an error occurs during the mapping. See the message of the exception for more details.
     */
    @Suppress("kotlin:S3776")
    fun <T : Any> mapToNullableClassK(tClass: KClass<T>, nullable: Boolean = false): T? {
        val clazzName = tClass.simpleName ?: throw ClassCastException("Class name is null")
        val sqlAlias = statement.tableAliases[tClass] ?: TableName.getAlias(tClass, manager.pool.configuration)

        val constructor = tClass.primaryConstructor ?: throw ClassCastException("$clazzName has no primary constructor")
        val parameters = constructor.parameters.filter { mp -> !ColumnIgnore.shouldIgnore(mp, IgnoreTarget.SELECT) }

        val retrievedParameterMap: MutableMap<KParameter, Any?> = mutableMapOf()

        for (parameter in parameters) {
            val name = ColumnName.getName(parameter, config)

            val parameterType = parameter.type

            val extractionMethod = ColumnGetType.getExtractionMethod(parameter)

            val foundValue = findValue(extractionMethod, sqlAlias, name)
                ?: if (parameter.isMarkedNullable()) {
                    retrievedParameterMap[parameter] = null
                    continue
                } else if (!parameter.isOptional) {
                    if (nullable) return null
                    else throw ClassCastException("Can't map $clazzName to $tClass because $name is null")
                } else continue

            val registeredCaster = config.casting.getCasterK(parameterType, foundValue::class.createType())
            val castedValue =
                registeredCaster?.fromQuery(foundValue, FromQuerySerDeContext(manager, parameterType))
                    ?: castEquivalent(parameter, foundValue)

            retrievedParameterMap[parameter] = castedValue
        }
        return constructor.callBy(retrievedParameterMap)
    }

    /**
     * Maps the result of the query to a class.
     *
     * If the query has not been executed yet, it is executed first.
     *
     * @param tClass The class to map to.
     *
     * @return A list of [T] containing the result of the mapping.
     * @throws ClassCastException If an error occurs during the mapping.
     * See the message of the exception for more details.
     */
    fun <T : Any> mapToClassK(tClass: KClass<T>): T {
        return mapToNullableClassK(tClass)!!
    }

    /**
     * Behaves like [mapToClassK], but instead of passing the class as a normal parameter, it is passed as a reified type.
     *
     * @see mapToClassK
     */
    inline fun <reified T : Any> mapToClass(): T = mapToClassK(T::class)

    /**
     * Maps the result of the query to a wrapper class.
     *
     * Each property of the wrapper class is mapped to the result of [mapToClassK].
     *
     * @param T The wrapper class to map to.
     *
     * @return A list of [T] containing the result of the mapping.
     * @throws ClassCastException If an error occurs during the mapping.
     */
    inline fun <reified T : Any> multiMapToClass(): T {
        val clazz = T::class

        val constructor = clazz.primaryConstructor
            ?: throw ClassCastException("${clazz.simpleName} has no primary constructor")
        val parameters = constructor.parameters

        val parameterValueMap = parameters.associateWith {
            try {
                val columnIndex = getColumnIndexOrNull(it)
                when (it.type) {
                    typeOf<Int?>() -> assertAndFind(columnIndex, it, ::mapToNullableInt)
                    typeOf<Int>() -> assertAndFind(columnIndex, it, ::mapToInt)
                    typeOf<Long?>() -> assertAndFind(columnIndex, it, ::mapToNullableLong)
                    typeOf<Long>() -> assertAndFind(columnIndex, it, ::mapToLong)
                    typeOf<Short?>() -> assertAndFind(columnIndex, it, ::mapToNullableShort)
                    typeOf<Short>() -> assertAndFind(columnIndex, it, ::mapToShort)
                    typeOf<Byte?>() -> assertAndFind(columnIndex, it, ::mapToNullableByte)
                    typeOf<Byte>() -> assertAndFind(columnIndex, it, ::mapToByte)
                    typeOf<Float?>() -> assertAndFind(columnIndex, it, ::mapToNullableFloat)
                    typeOf<Float>() -> assertAndFind(columnIndex, it, ::mapToFloat)
                    typeOf<Double?>() -> assertAndFind(columnIndex, it, ::mapToNullableDouble)
                    typeOf<Double>() -> assertAndFind(columnIndex, it, ::mapToDouble)
                    typeOf<BigDecimal?>() -> assertAndFind(columnIndex, it, ::mapToNullableBigDecimal)
                    typeOf<BigDecimal>() -> assertAndFind(columnIndex, it, ::mapToBigDecimal)
                    typeOf<Boolean?>() -> assertAndFind(columnIndex, it, ::mapToNullableBoolean)
                    typeOf<Boolean>() -> assertAndFind(columnIndex, it, ::mapToBoolean)
                    typeOf<String?>() -> assertAndFind(columnIndex, it, ::mapToNullableString)
                    typeOf<String>() -> assertAndFind(columnIndex, it, ::mapToString)
                    else -> mapToNullableClassK(it.asKClass(), it.isNullOrOptional())
                }
            } catch (e: ClassCastException) {
                throw ClassCastException(
                    "An exception occurred while mapping ${it.asKClass().simpleName}. " +
                            "Did you forget to make the property nullable?\n" +
                            "The exception's message: ${e.message}"
                )
            }
        }

        return constructor.callBy(parameterValueMap)
    }

    fun getColumnIndexOrNull(it: KParameter) = try {
        resultSet.findColumn(it.name ?: throw ClassCastException("Found a parameter without a name"))
    } catch (e: SQLException) {
        null
    }

    fun <T> assertAndFind(columnIndex: Int?, it: KParameter, f: (Int) -> T): T =
        if (columnIndex != null) f(columnIndex)
        else throw ClassCastException("Cant find column ${it.name} in the result set")

    private fun findValue(
        columnExtraction: ColumnExtraction<out Any?>,
        sqlAlias: String,
        name: String
    ) = (getResultSetValue(columnExtraction, resultSet, "$sqlAlias.$name")
        ?: getResultSetValue(columnExtraction, resultSet, "${sqlAlias}_$name"))
        ?: getResultSetValue(columnExtraction, resultSet, name)

    fun mapToNullableInt(index: Int = 1) = resultSet.getInt(index).takeIf { !resultSet.wasNull() }
    fun mapToInt(index: Int = 1) = mapToNullableInt(index) ?: throwBecauseNull()
    fun mapToNullableLong(index: Int = 1) = resultSet.getLong(index).takeIf { !resultSet.wasNull() }
    fun mapToLong(index: Int = 1) = mapToNullableLong(index) ?: throwBecauseNull()
    fun mapToNullableShort(index: Int = 1) = resultSet.getShort(index).takeIf { !resultSet.wasNull() }
    fun mapToShort(index: Int = 1) = mapToNullableShort(index) ?: throwBecauseNull()
    fun mapToNullableByte(index: Int = 1) = resultSet.getByte(index).takeIf { !resultSet.wasNull() }
    fun mapToByte(index: Int = 1) = mapToNullableByte(index) ?: throwBecauseNull()
    fun mapToNullableFloat(index: Int = 1) = resultSet.getFloat(index).takeIf { !resultSet.wasNull() }
    fun mapToFloat(index: Int = 1) = mapToNullableFloat(index) ?: throwBecauseNull()
    fun mapToNullableDouble(index: Int = 1) = resultSet.getDouble(index).takeIf { !resultSet.wasNull() }
    fun mapToDouble(index: Int = 1) = mapToNullableDouble(index) ?: throwBecauseNull()
    fun mapToNullableBigDecimal(index: Int = 1) = resultSet.getBigDecimal(index).takeIf { !resultSet.wasNull() }
    fun mapToBigDecimal(index: Int = 1) = mapToNullableBigDecimal(index) ?: throwBecauseNull()
    fun mapToNullableBoolean(index: Int = 1) = resultSet.getBoolean(index).takeIf { !resultSet.wasNull() }
    fun mapToBoolean(index: Int = 1) = mapToNullableBoolean(index) ?: throwBecauseNull()
    fun mapToNullableString(index: Int = 1) = resultSet.getString(index).takeIf { !resultSet.wasNull() }
    fun mapToString(index: Int = 1) = mapToNullableString(index) ?: throwBecauseNull()

    fun mapToNullableInt(name: String) = resultSet.getInt(name).takeIf { !resultSet.wasNull() }
    fun mapToInt(name: String) = mapToNullableInt(name) ?: throwBecauseNull()
    fun mapToNullableLong(name: String) = resultSet.getLong(name).takeIf { !resultSet.wasNull() }
    fun mapToLong(name: String) = mapToNullableLong(name) ?: throwBecauseNull()
    fun mapToNullableShort(name: String) = resultSet.getShort(name).takeIf { !resultSet.wasNull() }
    fun mapToShort(name: String) = mapToNullableShort(name) ?: throwBecauseNull()
    fun mapToNullableByte(name: String) = resultSet.getByte(name).takeIf { !resultSet.wasNull() }
    fun mapToByte(name: String) = mapToNullableByte(name) ?: throwBecauseNull()
    fun mapToNullableFloat(name: String) = resultSet.getFloat(name).takeIf { !resultSet.wasNull() }
    fun mapToFloat(name: String) = mapToNullableFloat(name) ?: throwBecauseNull()
    fun mapToNullableDouble(name: String) = resultSet.getDouble(name).takeIf { !resultSet.wasNull() }
    fun mapToDouble(name: String) = mapToNullableDouble(name) ?: throwBecauseNull()
    fun mapToNullableBigDecimal(name: String) = resultSet.getBigDecimal(name).takeIf { !resultSet.wasNull() }
    fun mapToBigDecimal(name: String) = mapToNullableBigDecimal(name) ?: throwBecauseNull()
    fun mapToNullableBoolean(name: String) = resultSet.getBoolean(name).takeIf { !resultSet.wasNull() }
    fun mapToBoolean(name: String) = mapToNullableBoolean(name) ?: throwBecauseNull()
    fun mapToNullableString(name: String) = resultSet.getString(name).takeIf { !resultSet.wasNull() }
    fun mapToString(name: String) = mapToNullableString(name) ?: throwBecauseNull()

    private fun throwBecauseNull(): Nothing {
        throw SQLException("Not null value expected, but null found")
    }

    companion object {
        private fun getResultSetValue(
            columnExtraction: ColumnExtraction<*>,
            resultSet: ResultSet,
            columnName: String
        ): Any? {
            return try {
                columnExtraction.extract(resultSet, columnName)
            } catch (_: SQLException) {
                null
            }
        }
    }
}