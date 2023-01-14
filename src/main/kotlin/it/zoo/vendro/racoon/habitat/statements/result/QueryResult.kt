package it.zoo.vendro.racoon.habitat.statements.result

import it.zoo.vendro.racoon.habitat.statements.QueryStatement
import java.sql.ResultSet

class QueryResult(
    val statement: QueryStatement,
    val resultSet: ResultSet
) : AutoCloseable {
    var isClosed = false
        private set

    override fun close() {
        if (!isClosed) {
            resultSet.close()
            isClosed = true
        }
    }

    fun assertNotClosed() {
        if (isClosed) throw IllegalStateException("The result is closed")
    }

    fun getNextRow(): QueryResultRow? {
        assertNotClosed()
        return if (resultSet.next()) QueryResultRow(this) else null
    }

    fun <T> consumeRows(processor: (QueryResultRow) -> T): List<T> = use {
        assertNotClosed()
        val processList = mutableListOf<T>()
        while (true) {
            val row = getNextRow() ?: break
            processList.add(processor(row))
        }
        return processList
    }
}