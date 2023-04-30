package it.zoo.vendro.racoon.internals.configuration

import it.zoo.vendro.racoon.internals.protocols.MySQLProtocol
import it.zoo.vendro.racoon.internals.protocols.SQLProtocol

/**
 * The settings used to configure the connection to the database.
 */
data class ConnectionSettings(
    /**
     * The hostname of the database.
     */
    val host: String,
    /**
     * The port of the database.
     */
    val port: Int = 3306,
    /**
     * The database name.
     */
    val database: String,
    /**
     * The username to use when connecting to the database.
     */
    val username: String? = null,
    /**
     * The password to use when connecting to the database.
     */
    val password: String? = null,
    /**
     * The timeout in milliseconds to use when connecting to the database.
     *
     * The default value is 100.
     */
    val connectionTimeout: Int = 100,
    /**
     * The timeout in milliseconds to use when waiting for a response from the database.
     *
     * The default value is 2000.
     */
    val socketTimeout: Int = 2000,
    /**
     * The number of seconds to wait before closing an idle connection.
     *
     * If the number is less than or equal to 0, the connection expires according to the default database settings.
     *
     * The default value is 30.
     */
    val idleTimeout: Int = 30,
    /**
     * The maximum number of connections to keep open at any one time.
     *
     * If this value is set to 0, then there is no limit.
     *
     * The default value is 0.
     */
    val maxManagers: Int = 0,
    /**
     * The maximum number of connections to keep stored in the pool.
     *
     * If the pool already has this many connections, then new connections will be closed.
     *
     * The default value is 10.
     */
    val maxPoolSize: Int = 10,
    /**
     * The name of the protocol.
     *
     * The default value is "mysql".
     *
     * NOTE: Values other than "mysql" are not supported, and so may not work.
     */
    val protocol: SQLProtocol = MySQLProtocol()
) {
    /**
     * Creates a connection string for the database.
     *
     * @return The connection string.
     */
    override fun toString(): String {
        val stringBuilder = StringBuilder("jdbc:${protocol.name}://")
            .append("$host:$port/$database")
            .append("?characterEncoding=UTF-8&")
            .append("connectionTimeout=$connectionTimeout&")
            .append("socketTimeout=$socketTimeout")

        if (username != null) stringBuilder.append("&user=$username")
        if (password != null) stringBuilder.append("&password=$password")

        return stringBuilder.toString()
    }
}