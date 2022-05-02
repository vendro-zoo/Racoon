import commons.ConnectionSettings
import commons.RacoonConfiguration
import habitat.RacoonManager
import models.Cat
import org.intellij.lang.annotations.Language

fun main() {
    RacoonConfiguration.defaultConnectionSettings = ConnectionSettings(
        host = "localhost",
        port = 3306,
        database = "test",
        username = "test",
        password = "test"
    )

    val racoonManager = RacoonManager.create()

    @Language("MySQL") val racoon = racoonManager.createRacoon("SELECT c.* FROM cat c")
    val cats = racoon.mapToClass<Cat>()

    println(cats.first().name)
}