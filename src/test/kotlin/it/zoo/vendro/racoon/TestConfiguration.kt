package it.zoo.vendro.racoon

import it.zoo.vendro.racoon.habitat.ConnectionPool
import it.zoo.vendro.racoon.habitat.configuration.RacoonConfiguration
import it.zoo.vendro.racoon.internals.configuration.ConnectionSettings
import it.zoo.vendro.racoon.internals.mappers.NameMapper
import it.zoo.vendro.racoon.internals.protocols.PostgresSQLProtocol

object TestConfiguration {
    val CONFIGURATION = RacoonConfiguration(
        connection = RacoonConfiguration.Connection(
            ConnectionSettings(
                host = "localhost",
                port = 5432,
                database = "racoon",
                username = "admin",
                password = "admin",
                protocol = PostgresSQLProtocol(),
                idleTimeout = 0
            )
        ),
        naming = RacoonConfiguration.Naming(
            tableNameMapper = NameMapper.lowerSnakeCase,
            tableAliasMapper = NameMapper.lowerSnakeCase
        ),
    )
    val POOL = ConnectionPool(
        CONFIGURATION
    )
}