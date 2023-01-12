package it.zoo.vendro.racoon.habitat

import it.zoo.vendro.racoon.habitat.configuration.RacoonConfiguration
import it.zoo.vendro.racoon.internals.extensions.removeLastOrNull
import it.zoo.vendro.racoon.internals.query.Ping
import java.sql.Connection
import java.sql.SQLException
import java.util.concurrent.ConcurrentLinkedDeque

class ConnectionPool(val configuration: RacoonConfiguration) {
    private val availableConnections: ConcurrentLinkedDeque<Connection> = ConcurrentLinkedDeque()
    private val unavailableManagers: MutableSet<ConnectionManager> = mutableSetOf()

    /**
     * Returns the number of in-use [ConnectionManager]s.
     *
     * @return the number of in-use [ConnectionManager]s
     */
    fun inUseManagers(): Int = unavailableManagers.size

    /**
     * Returns the number of available [Connection]s.
     *
     * Not all the connections may be available. Some may be expired.
     *
     * @return the number of available [Connection]s
     */
    fun notInUseManagers(): Int = availableConnections.size

    /**
     * Returns a manager that is available for use.
     * If no managers are available in the pool, a new one is created.
     *
     * @return A manager that is available for use.
     * @throws SQLException If the number of available managers exceeds the maximum number of managers.
     */
    fun getManager(): ConnectionManager {
        val settings = configuration.connection.connectionSettings

        if (settings.maxManagers != 0 &&
            availableConnections.isEmpty() &&
            unavailableManagers.size >= settings.maxManagers)
            throw SQLException("The maximum number of managers has been reached")


        // If there are available managers, return the first still available one, if exists
        do {
            // Get the first available manager
            val c = availableConnections.removeLastOrNull() ?: break
            val manager = ConnectionManager(c, this)

            // Check if the manager is still available, if not, check the next one
            if (!ping(manager)) continue

            // If the manager is available, add it to the unavailable list
            unavailableManagers.add(manager)

            return manager
        } while (true)

        // Return a new manager
        val manager = ConnectionManager.fromSettings(settings, this)
        unavailableManagers.add(manager)
        return manager
    }

    /**
     * Re-inserts a manager into the pool.
     *
     * @param manager The manager to re-insert.
     * @return True if the manager was re-inserted, false otherwise.
     */
    fun releaseManager(manager: ConnectionManager): Boolean {
        // Moves the manager to the available list
        if (availableConnections.size >= configuration.connection.connectionSettings.maxPoolSize) {
            manager.connection.close()
            return false
        }
        unavailableManagers.remove(manager)
        availableConnections.addLast(manager.connection)
        return true
    }

    /**
     * Checks if the manager is available by running a ping query.
     *
     * @param manager The manager to check
     * @return True if the manager is available, false otherwise
     */
    private fun ping(manager: ConnectionManager): Boolean {
        try {
            // Executing a ping query to the database to check if the connection is still alive
            val ping = manager.createQuery("SELECT 1 ping").mapToClass<Ping>()

            // If the result is not 1, the connection is not alive
            if (ping.isEmpty() || ping[0].ping != 1.toByte()) return false
        } catch (e: SQLException) {
            // If an exception is thrown, the connection is not alive
            return false
        }

        // Otherwise, the connection is alive
        return true
    }
}