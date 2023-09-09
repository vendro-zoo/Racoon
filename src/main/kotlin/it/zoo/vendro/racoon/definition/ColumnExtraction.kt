@file:Suppress("unused")

package it.zoo.vendro.racoon.definition

import java.io.InputStream
import java.io.Reader
import java.math.BigDecimal
import java.sql.Date
import java.sql.ResultSet
import java.sql.Time
import java.sql.Timestamp

/**
 * A sealed class used to extract a value from a [ResultSet] based on the column name.
 *
 * Use the annotation [ColumnGetType] with a [ColumnGetTypes] to define
 * how a value should be extracted from a [ResultSet].
 *
 * This class is used internally to make the annotation work.
 *
 * @param T The type of the value to extract.
 */
sealed class ColumnExtraction<T> {
    abstract fun extract(rs: ResultSet, columnName: kotlin.String): T

    object String : ColumnExtraction<kotlin.String?>() {
        override fun extract(rs: ResultSet, columnName: kotlin.String): kotlin.String? {
            return rs.getString(columnName)
        }
    }

    object Boolean : ColumnExtraction<kotlin.Boolean?>() {
        override fun extract(rs: ResultSet, columnName: kotlin.String): kotlin.Boolean? {
            return rs.getBoolean(columnName)
        }
    }

    object Byte : ColumnExtraction<kotlin.Byte?>() {
        override fun extract(rs: ResultSet, columnName: kotlin.String): kotlin.Byte? {
            return rs.getByte(columnName)
        }
    }

    object Short : ColumnExtraction<kotlin.Short?>() {
        override fun extract(rs: ResultSet, columnName: kotlin.String): kotlin.Short? {
            return rs.getShort(columnName)
        }
    }

    object Int : ColumnExtraction<kotlin.Int?>() {
        override fun extract(rs: ResultSet, columnName: kotlin.String): kotlin.Int? {
            return rs.getInt(columnName)
        }
    }

    object Long : ColumnExtraction<kotlin.Long?>() {
        override fun extract(rs: ResultSet, columnName: kotlin.String): kotlin.Long? {
            return rs.getLong(columnName)
        }
    }

    object Float : ColumnExtraction<kotlin.Float?>() {
        override fun extract(rs: ResultSet, columnName: kotlin.String): kotlin.Float? {
            return rs.getFloat(columnName)
        }
    }

    object Double : ColumnExtraction<kotlin.Double?>() {
        override fun extract(rs: ResultSet, columnName: kotlin.String): kotlin.Double? {
            return rs.getDouble(columnName)
        }
    }

    object BigDecimal : ColumnExtraction<java.math.BigDecimal?>() {
        override fun extract(rs: ResultSet, columnName: kotlin.String): java.math.BigDecimal? {
            return rs.getBigDecimal(columnName)
        }
    }

    object Bytes : ColumnExtraction<ByteArray?>() {
        override fun extract(rs: ResultSet, columnName: kotlin.String): ByteArray? {
            return rs.getBytes(columnName)
        }
    }

    object Date : ColumnExtraction<java.sql.Date?>() {
        override fun extract(rs: ResultSet, columnName: kotlin.String): java.sql.Date? {
            return rs.getDate(columnName)
        }
    }

    object Time : ColumnExtraction<java.sql.Time?>() {
        override fun extract(rs: ResultSet, columnName: kotlin.String): java.sql.Time? {
            return rs.getTime(columnName)
        }
    }

    object Timestamp : ColumnExtraction<java.sql.Timestamp?>() {
        override fun extract(rs: ResultSet, columnName: kotlin.String): java.sql.Timestamp? {
            return rs.getTimestamp(columnName)
        }
    }

    object AsciiStream : ColumnExtraction<InputStream?>() {
        override fun extract(rs: ResultSet, columnName: kotlin.String): InputStream? {
            return rs.getAsciiStream(columnName)
        }
    }

    object BinaryStream : ColumnExtraction<InputStream?>() {
        override fun extract(rs: ResultSet, columnName: kotlin.String): InputStream? {
            return rs.getBinaryStream(columnName)
        }
    }

    object CharacterStream : ColumnExtraction<Reader?>() {
        override fun extract(rs: ResultSet, columnName: kotlin.String): Reader? {
            return rs.getCharacterStream(columnName)
        }
    }

    object Object : ColumnExtraction<Any?>() {
        override fun extract(rs: ResultSet, columnName: kotlin.String): Any? {
            return rs.getObject(columnName)
        }
    }
}

enum class ColumnGetTypes(val extractor: ColumnExtraction<*>) {
    String(ColumnExtraction.String),
    Boolean(ColumnExtraction.Boolean),
    Byte(ColumnExtraction.Byte),
    Short(ColumnExtraction.Short),
    Int(ColumnExtraction.Int),
    Long(ColumnExtraction.Long),
    Float(ColumnExtraction.Float),
    Double(ColumnExtraction.Double),
    BigDecimal(ColumnExtraction.BigDecimal),
    Bytes(ColumnExtraction.Bytes),
    Date(ColumnExtraction.Date),
    Time(ColumnExtraction.Time),
    Timestamp(ColumnExtraction.Timestamp),
    AsciiStream(ColumnExtraction.AsciiStream),
    BinaryStream(ColumnExtraction.BinaryStream),
    CharacterStream(ColumnExtraction.CharacterStream),
    Object(ColumnExtraction.Object)
}