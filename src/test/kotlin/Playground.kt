import commons.configuration.ConnectionSettings
import commons.configuration.RacoonConfiguration
import habitat.RacoonManager
import models.Cat
import models.Owner

fun main() {
    RacoonConfiguration.defaultConnectionSettings = ConnectionSettings(
        host = "localhost",
        port = 3306,
        database = "test",
        username = "test",
        password = "test"
    )
    data class Wrapper(val cat: Cat, val owner: Owner)

    RacoonManager.create().use { racoonManager ->
        val mapped = racoonManager.createRacoon("SELECT c.*, o.* FROM cat c, owner o")
            .use {
                print(it.getImplementationResult(false))
                it.mutliMapToClass<Wrapper>()
            }
        println(mapped.first())
    }

}