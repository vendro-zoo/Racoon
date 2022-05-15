package habitat.racoons

import commons.query.QueryProcessing
import habitat.RacoonManager

class InsertRacoon(manager: RacoonManager, query: String, var lastId: Int? = null) : Racoon<InsertRacoon>(manager, query) {
    override fun execute() = apply {
        val queryProcessingResult = QueryProcessing.reconstructQuery(originalQuery)

        val processedQuery = queryProcessingResult.first
        indexedParametersMappings = queryProcessingResult.second
        namedParametersMappings = queryProcessingResult.third

        preparedStatement = manager.prepare(processedQuery)

        bindParameters()

        preparedStatement!!.executeUpdate()

        lastId = manager.getLastId()
    }
}