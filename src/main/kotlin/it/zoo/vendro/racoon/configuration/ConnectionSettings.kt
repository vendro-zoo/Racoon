package it.zoo.vendro.racoon.configuration

import it.zoo.vendro.racoon.protocols.MySQLProtocol
import it.zoo.vendro.racoon.protocols.SQLProtocol

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
    override val username: String? = null,
    /**
     * The password to use when connecting to the database.
     */
    override val password: String? = null,
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
    override val idleTimeout: Int = 30,
    /**
     * The maximum number of connections to keep open at any one time.
     *
     * If this value is set to 0, then there is no limit.
     *
     * The default value is 0.
     */
    override val maxManagers: Int = 0,
    /**
     * The maximum number of connections to keep stored in the pool.
     *
     * If the pool already has this many connections, then new connections will be closed.
     *
     * The default value is 10.
     */
    override val maxPoolSize: Int = 10,
    /**
     * The name of the protocol.
     *
     * The default value is "mysql".
     *
     * NOTE: Values other than "mysql" are not supported, and so may not work.
     */
    override val protocol: SQLProtocol = MySQLProtocol()
) : JDBCConnectionString {
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

    override fun getConnectionString(): String = toString()
}

data class RawConnectionSettings(
    val url: String,
    override val username: String? = null,
    override val password: String? = null,
    override val maxManagers: Int = 0,
    override val maxPoolSize: Int = 10,
    override val protocol: SQLProtocol = MySQLProtocol()
) : JDBCConnectionString {
    override fun getConnectionString(): String = url
    override val idleTimeout: Int = 0
}

interface JDBCConnectionString {
    val username: String?
    val password: String?
    val idleTimeout: Int
    val maxManagers: Int
    val maxPoolSize: Int
    val protocol: SQLProtocol
    fun getConnectionString(): String
}