@file:Suppress("unused")

package it.zoo.vendro.racoon.definition

import java.sql.PreparedStatement
import java.sql.Types

/**
 * A sealed class used to insert a value into a [PreparedStatement].
 *
 * Use the annotation [ColumnGetType] with a [ColumnGetTypes] to define
 * how a value should be inserted into a [PreparedStatement].
 *
 * This class is used internally to make the annotation work.
 *
 * @param T The type of the value to insert.
 */
sealed class ColumnInsertion {
    abstract fun insert(rs: PreparedStatement, index: kotlin.Int, value: Any?)

    object Object : ColumnInsertion() {
        override fun insert(rs: PreparedStatement, index: kotlin.Int, value: Any?) {
            return rs.setObject(index, value)
        }
    }

    object ObjectOther : ColumnInsertion() {
        override fun insert(rs: PreparedStatement, index: kotlin.Int, value: Any?) {
            return rs.setObject(index, value, Types.OTHER)
        }
    }
}

enum class ColumnSetTypes(val inserter: ColumnInsertion) {
    Object(ColumnInsertion.Object),
    ObjectOther(ColumnInsertion.ObjectOther)
}