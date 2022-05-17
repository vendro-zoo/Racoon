package habitat.racoons

import habitat.RacoonManager

class InsertRacoon(manager: RacoonManager, query: String, var lastId: Int? = null) : ExecuteRacoon(manager, query) {
    override fun execute() = apply {
        // Executes query
        super.execute()

        // Saves the last inserted id
        lastId = manager.getLastId()
    }
}