import commons.configuration.ConnectionSettings
import commons.configuration.RacoonConfiguration
import habitat.RacoonManager
import models.Cat

fun main() {
    RacoonConfiguration.Connection.setDefault(ConnectionSettings(
        host = "localhost",
        port = 3306,
        database = "test",
        username = "test",
        password = "test"
    ))

    RacoonManager.create().use { racoonManager ->
        val mapped = racoonManager.createQueryRacoon("SELECT c.* FROM cat c WHERE c.name = :name")
            .use {
                it.setParam("name", "Carl")
                it.mapToClass<Cat>()
            }
        println(mapped.first())
    }

}