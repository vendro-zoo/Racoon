package habitat.racoons

import habitat.RacoonManager

/**
 * Behaves like an [ExecuteRacoon], but also stores the last inserted id.
 */
class InsertRacoon(manager: RacoonManager, query: String) : ExecuteRacoon(manager, query) {
    /**
     * The id of the last inserted row.
     *
     * If the query has not been executed yet, this will be `null`.
     */
    var lastId: Int? = null

    /**
     * Executes the query to the database and saves the last inserted id.
     */
    override fun execute() = apply {
        // Executes query
        super.execute()

        // Saves the last inserted id
        lastId = manager.getLastId()
    }
}