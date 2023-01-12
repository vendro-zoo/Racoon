package it.zoo.vendro.racoon.habitat.racoons

import it.zoo.vendro.racoon.habitat.ConnectionManager
import it.zoo.vendro.racoon.internals.query.QueryProcessing

/**
 * A [Statement] capable of handling DML queries. No result is returned.
 */
open class ExecuteStatement(manager: ConnectionManager, query: String) : Statement<ExecuteStatement>(manager, query) {
    override fun execute() = apply {
        val queryProcessingResult = QueryProcessing.reconstructQuery(originalQuery, parameters)

        val processedQuery = queryProcessingResult.first
        parameterMapping = queryProcessingResult.second

        preparedStatement = manager.prepare(processedQuery)

        bindParameters()

        preparedStatement!!.executeUpdate()
    }
}