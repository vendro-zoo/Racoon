package habitat.racoons

import commons.query.QueryProcessing
import habitat.RacoonManager

open class ExecuteRacoon(manager: RacoonManager, query: String) : Racoon<ExecuteRacoon>(manager, query) {
    override fun execute() = apply {
        val queryProcessingResult = QueryProcessing.reconstructQuery(originalQuery)

        val processedQuery = queryProcessingResult.first
        indexedParametersMappings = queryProcessingResult.second
        namedParametersMappings = queryProcessingResult.third

        preparedStatement = manager.prepare(processedQuery)

        bindParameters()

        preparedStatement!!.executeUpdate()
    }
}