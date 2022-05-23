package commons.casting

import java.sql.ResultSet

@FunctionalInterface
interface RecordCaster<T> {
    fun cast(resultSet: ResultSet): T
}