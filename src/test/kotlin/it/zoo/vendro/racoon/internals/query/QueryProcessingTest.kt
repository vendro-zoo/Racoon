package it.zoo.vendro.racoon.internals.query

import it.zoo.vendro.racoon.habitat.ConnectionPool
import it.zoo.vendro.racoon.habitat.configuration.RacoonConfiguration
import it.zoo.vendro.racoon.habitat.statements.parameters.Parameters
import it.zoo.vendro.racoon.internals.configuration.ConnectionSettings
import it.zoo.vendro.racoon.internals.mappers.NameMapper
import it.zoo.vendro.racoon.internals.mappers.TableAliasMapper
import it.zoo.vendro.racoon.internals.protocols.PostgresSQLProtocol
import it.zoo.vendro.racoon.internals.query.QueryProcessing.reconstructQuery
import org.junit.jupiter.api.Test

class QueryProcessingTest {
    val CONNECTION_SETTINGS = ConnectionSettings(
        host = "localhost",
        port = 5432,
        database = "squirrel",
        username = "omni-squirrel",
        password = "omni-squirrel",
        protocol = PostgresSQLProtocol(),
        idleTimeout = 0
    )

    val CONNECTION_POOL = ConnectionPool(
        configuration = RacoonConfiguration(
            connection = RacoonConfiguration.Connection(CONNECTION_SETTINGS),
            naming = RacoonConfiguration.Naming(
                tableAliasMapper = TableAliasMapper.onlyUpperNonSequentialToLower,
                columnNameMapper = NameMapper.lowerSnakeCase,
                tableNameMapper = NameMapper.lowerSnakeCase
            ),
        )
    )

    @Test
    fun t() {
        val r = CONNECTION_POOL.getManager().use { cm ->
            val parameters = Parameters(cm).apply {
                setParam(1, "test")
            }
            reconstructQuery("SELECT ?1", parameters, cm.config)
//            QueryProcessing.calculateMatches("SELECT ?1", cm.config)
        }
        println(r)
    }
}