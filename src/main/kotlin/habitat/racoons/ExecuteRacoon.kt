package habitat.racoons

import habitat.RacoonManager
import internals.query.QueryProcessing

/**
 * A [Racoon] capable of handling DML queries. No result is returned.
 */
open class ExecuteRacoon(manager: RacoonManager, query: String) : Racoon<ExecuteRacoon>(manager, query) {
    override fun execute() = apply {
        val queryProcessingResult = QueryProcessing.reconstructQuery(originalQuery, parameters)

        val processedQuery = queryProcessingResult.first
        parameterMapping = queryProcessingResult.second

        preparedStatement = manager.prepare(processedQuery)

        bindParameters()

        preparedStatement!!.executeUpdate()
    }
}