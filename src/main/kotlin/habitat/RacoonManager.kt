package habitat

import commons.configuration.ConnectionSettings
import commons.configuration.RacoonConfiguration
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement

class RacoonManager(private val connection: Connection) : AutoCloseable {
    /**
     * Closes the connection to the database.
     * This method should be called when the [RacoonManager] is no longer needed.
     */
    override fun close() {
        connection.close()
    }

    /**
     * @return a new scrollable [ResultSet].
     */
    private fun getStatement(): Statement {
        return connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
    }

    /**
     * Creates a [Racoon] with the given query.
     *
     * @param query The query to execute.
     * @return A [Racoon] with the given query.
     */
    fun createRacoon(query: String): Racoon {
        return Racoon(getStatement(), query)
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
        fun create(connectionSettings: ConnectionSettings? = RacoonConfiguration.defaultConnectionSettings): RacoonManager {
            // If no connection settings are defined, throw an exception
            if (connectionSettings == null)
                throw IllegalStateException(
                    "No connection settings provided " +
                            "and no default connection settings configured. " +
                            "Please provide connection settings or configure " +
                            "default connection settings by changing " +
                            "'commons.configuration.RacoonConfiguration.defaultConnectionSettings'."
                )

            // Return a new instance of the habitat.RacoonManager
            return RacoonManager(DriverManager.getConnection(connectionSettings.toString()))
        }
    }
}