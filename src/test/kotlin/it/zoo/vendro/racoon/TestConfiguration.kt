package it.zoo.vendro.racoon

import it.zoo.vendro.racoon.configuration.ConnectionSettings
import it.zoo.vendro.racoon.configuration.RacoonConfiguration
import it.zoo.vendro.racoon.configuration.RacoonConfiguration.Naming.Companion.lowerSnakeCase
import it.zoo.vendro.racoon.connection.ConnectionPool
import it.zoo.vendro.racoon.protocols.PostgresSQLProtocol
import it.zoo.vendro.racoon.test.PostgreContainer

object TestConfiguration {
    val CONNECTION = ConnectionSettings(
        host = PostgreContainer.POSTGRE.host,
        port = PostgreContainer.POSTGRE.getMappedPort(5432),
        database = "testdb",
        username = "testuser",
        password = "testpassword",
        protocol = PostgresSQLProtocol(),
        idleTimeout = 0
    )
    val CONFIGURATION = RacoonConfiguration(
        connection = CONNECTION,
        naming = RacoonConfiguration.Naming(
            tableNameMapper = lowerSnakeCase,
            tableAliasMapper = lowerSnakeCase
        ),
    )
    val POOL = ConnectionPool(
        CONFIGURATION
    )
}