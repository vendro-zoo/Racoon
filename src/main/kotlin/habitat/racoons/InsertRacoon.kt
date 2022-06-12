package habitat.racoons

import habitat.RacoonManager
import internals.query.QueryProcessing

/**
 * Behaves like an [ExecuteRacoon], but also stores the last inserted id.
 */
class InsertRacoon(manager: RacoonManager, query: String) : BatchableRacoon<InsertRacoon>(manager, query) {
    /**
     * The id of the inserted rows.
     *
     * If the query has not been executed yet, this will be empty.
     */
    val generatedKeys: MutableList<Int> = mutableListOf()

    override fun executeBatch() = apply {
        val queryProcessingResult = QueryProcessing.reconstructQuery(originalQuery)

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
        val queryProcessingResult = QueryProcessing.reconstructQuery(originalQuery)

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