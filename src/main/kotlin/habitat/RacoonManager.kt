package habitat

import commons.configuration.ConnectionSettings
import habitat.configuration.RacoonConfiguration
import habitat.racoons.QueryRacoon
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

class RacoonManager(private val connection: Connection) : AutoCloseable {
    /**
     * Closes the connection to the database.
     * This method should be called when the [RacoonManager] is no longer needed.
     */
    override fun close() {
        connection.close()
    }

    fun prepare(query: String): PreparedStatement {
        return connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
    }

    /**
     * Creates a [QueryRacoon] of type [RacoonType.QUERY] with the given query.
     *
     * @param query The query to execute.
     * @return A [QueryRacoon] capable of handling the query and its results.
     */
    fun createQueryRacoon(query: String): QueryRacoon {
        return QueryRacoon(this, query)
    }

    // Handles the instantiation of the class.
    // Can be considered the factory.
    companion object {
        /**
         * Creates a new instance of [RacoonManager] by creating a new connection to the database.
         * @param connectionSettings
         * The connection settings. If no connection settings are provided,
         * the default connection settings will be used.
         * @throws IllegalArgumentException
         * If no connection settings are provided and
         * the default connection settings are not available.
         */
        fun create(connectionSettings: ConnectionSettings? = RacoonConfiguration.Connection.getDefault()): RacoonManager {
            // If no connection settings are defined, throw an exception
            if (connectionSettings == null)
                throw IllegalStateException(
                    "No connection settings provided " +
                            "and no default connection settings configured. " +
                            "Please provide connection settings or configure " +
                            "default connection settings by calling " +
                            "'habitat.configuration.RacoonConfiguration.Connection.setDefault()'."
                )

            // Return a new instance of the habitat.RacoonManager
            return RacoonManager(DriverManager.getConnection(connectionSettings.toString()))
        }
    }
}