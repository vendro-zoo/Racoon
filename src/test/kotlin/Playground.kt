import commons.configuration.ConnectionSettings
import commons.configuration.RacoonConfiguration
import habitat.RacoonManager

fun main() {
    RacoonConfiguration.Connection.setDefault(ConnectionSettings(
        host = "localhost",
        port = 3306,
        database = "test",
        username = "test",
        password = "test"
    ))

    RacoonManager.create().use { racoonManager ->
        racoonManager.createRacoon("select 1 from cat c where c.name = (?, ?) and c.id = ? and p = :n1 and a in (:n2, :n3)")
            .use {
                it.setParam(1, 'a')
                    .setParam(2, 'b')
                    .setParam(3, 'c')
                    .setParam("n1", 'd')
                    .setParam("n2", 'e')
                    .setParam("n3", 'f')
                    .calculateProcessedQuery()
//                print(it.getImplementationResult(false))
//                it.mutliMapToClass<Wrapper>()
            }
//        println(mapped.first())
    }

}