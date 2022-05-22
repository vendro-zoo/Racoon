package habitat.racoons

import habitat.RacoonManager

/**
 * Behaves like an [ExecuteRacoon], but also stores the last inserted id.
 */
class InsertRacoon(manager: RacoonManager, query: String, var lastId: Int? = null) : ExecuteRacoon(manager, query) {
    override fun execute() = apply {
        // Executes query
        super.execute()

        // Saves the last inserted id
        lastId = manager.getLastId()
    }
}