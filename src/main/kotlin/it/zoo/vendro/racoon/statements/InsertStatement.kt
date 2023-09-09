package it.zoo.vendro.racoon.statements

import it.zoo.vendro.racoon.habitat.ConnectionManager
import it.zoo.vendro.racoon.internals.query.QueryProcessing

/**
 * Behaves like an [ExecuteStatement], but also stores the last inserted id.
 */
class InsertStatement(manager: ConnectionManager, query: String) : BatchableStatement<InsertStatement>(manager, query) {
    /**
     * The id of the inserted rows.
     *
     * If the query has not been executed yet, this will be empty.
     */
    val generatedKeys: MutableList<Int> = mutableListOf()

    override fun executeBatch() = apply {
        val queryProcessingResult = QueryProcessing.reconstructQuery(originalQuery, parameters, manager.config)

        val processedQuery = queryProcessingResult.first
        parameterMapping = queryProcessingResult.second

        preparedStatement = manager.prepareInserted(processedQuery)

        bindBatchParameters()

        preparedStatement!!.executeBatch()

        getKeys()
    }

    /**
     * Executes the query to the database and saves the last inserted id.
     */
    override fun execute() = apply {
        val queryProcessingResult = QueryProcessing.reconstructQuery(originalQuery, parameters, manager.config)

        val processedQuery = queryProcessingResult.first
        parameterMapping = queryProcessingResult.second

        preparedStatement = manager.prepareInserted(processedQuery)

        bindParameters()

        preparedStatement!!.executeUpdate()

        getKeys()
    }

    private fun getKeys() {
        val rawGeneratedKeys = preparedStatement!!.generatedKeys
        while (rawGeneratedKeys.next()) {
            generatedKeys.add(rawGeneratedKeys.getInt(1))
        }
    }
}