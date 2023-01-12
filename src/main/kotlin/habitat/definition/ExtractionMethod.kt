@file:Suppress("unused")

package habitat.definition

import java.sql.ResultSet

/**
 * A sealed class used to extract a value from a [ResultSet] based on the column name.
 *
 * Use the annotation [ColumnExtractionMethod] with a [ExtractionMethodType] to define
 * how a value should be extracted from a [ResultSet].
 *
 * This class is used internally to make the annotation work.
 *
 * @param T The type of the value to extract.
 */
sealed class ExtractionMethod<T> {
    abstract fun extract(rs: ResultSet, columnName: kotlin.String): T

    object String : ExtractionMethod<kotlin.String>() {
        override fun extract(rs: ResultSet, columnName: kotlin.String): kotlin.String {
            return rs.getString(columnName)
        }
    }

    object Boolean : ExtractionMethod<kotlin.Boolean>() {
        override fun extract(rs: ResultSet, columnName: kotlin.String): kotlin.Boolean {
            return rs.getBoolean(columnName)
        }
    }

    object Byte : ExtractionMethod<kotlin.Byte>() {
        override fun extract(rs: ResultSet, columnName: kotlin.String): kotlin.Byte {
            return rs.getByte(columnName)
        }
    }

    object Short : ExtractionMethod<kotlin.Short>() {
        override fun extract(rs: ResultSet, columnName: kotlin.String): kotlin.Short {
            return rs.getShort(columnName)
        }
    }

    object Int : ExtractionMethod<kotlin.Int>() {
        override fun extract(rs: ResultSet, columnName: kotlin.String): kotlin.Int {
            return rs.getInt(columnName)
        }
    }

    object Long : ExtractionMethod<kotlin.Long>() {
        override fun extract(rs: ResultSet, columnName: kotlin.String): kotlin.Long {
            return rs.getLong(columnName)
        }
    }

    object Float : ExtractionMethod<kotlin.Float>() {
        override fun extract(rs: ResultSet, columnName: kotlin.String): kotlin.Float {
            return rs.getFloat(columnName)
        }
    }

    object Double : ExtractionMethod<kotlin.Double>() {
        override fun extract(rs: ResultSet, columnName: kotlin.String): kotlin.Double {
            return rs.getDouble(columnName)
        }
    }

    object BigDecimal : ExtractionMethod<java.math.BigDecimal>() {
        override fun extract(rs: ResultSet, columnName: kotlin.String): java.math.BigDecimal {
            return rs.getBigDecimal(columnName)
        }
    }

    object Bytes : ExtractionMethod<ByteArray>() {
        override fun extract(rs: ResultSet, columnName: kotlin.String): ByteArray {
            return rs.getBytes(columnName)
        }
    }

    object Date : ExtractionMethod<java.sql.Date>() {
        override fun extract(rs: ResultSet, columnName: kotlin.String): java.sql.Date {
            return rs.getDate(columnName)
        }
    }

    object Time : ExtractionMethod<java.sql.Time>() {
        override fun extract(rs: ResultSet, columnName: kotlin.String): java.sql.Time {
            return rs.getTime(columnName)
        }
    }

    object Timestamp : ExtractionMethod<java.sql.Timestamp>() {
        override fun extract(rs: ResultSet, columnName: kotlin.String): java.sql.Timestamp {
            return rs.getTimestamp(columnName)
        }
    }

    object AsciiStream : ExtractionMethod<java.io.InputStream>() {
        override fun extract(rs: ResultSet, columnName: kotlin.String): java.io.InputStream {
            return rs.getAsciiStream(columnName)
        }
    }

    object BinaryStream : ExtractionMethod<java.io.InputStream>() {
        override fun extract(rs: ResultSet, columnName: kotlin.String): java.io.InputStream {
            return rs.getBinaryStream(columnName)
        }
    }

    object CharacterStream : ExtractionMethod<java.io.Reader>() {
        override fun extract(rs: ResultSet, columnName: kotlin.String): java.io.Reader {
            return rs.getCharacterStream(columnName)
        }
    }

    object Object : ExtractionMethod<kotlin.Any?>() {
        override fun extract(rs: ResultSet, columnName: kotlin.String): kotlin.Any? {
            return rs.getObject(columnName)
        }
    }
}

enum class ExtractionMethodType(val extractor: ExtractionMethod<*>) {
    String(ExtractionMethod.String),
    Boolean(ExtractionMethod.Boolean),
    Byte(ExtractionMethod.Byte),
    Short(ExtractionMethod.Short),
    Int(ExtractionMethod.Int),
    Long(ExtractionMethod.Long),
    Float(ExtractionMethod.Float),
    Double(ExtractionMethod.Double),
    BigDecimal(ExtractionMethod.BigDecimal),
    Bytes(ExtractionMethod.Bytes),
    Date(ExtractionMethod.Date),
    Time(ExtractionMethod.Time),
    Timestamp(ExtractionMethod.Timestamp),
    AsciiStream(ExtractionMethod.AsciiStream),
    BinaryStream(ExtractionMethod.BinaryStream),
    CharacterStream(ExtractionMethod.CharacterStream),
    Object(ExtractionMethod.Object)
}