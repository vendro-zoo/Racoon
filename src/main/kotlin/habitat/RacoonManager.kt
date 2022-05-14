package habitat

import commons.configuration.ConnectionSettings
import habitat.racoons.QueryRacoon
import org.intellij.lang.annotations.Language
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

class RacoonManager(
    private val connection: Connection
) {
    /**
     * Closes the connection to the database.
     * This method should be called when the [RacoonManager] is no longer needed.
     */
    internal fun close() {
        connection.close()
    }

    /**
     * Executes the given block and then releases the manager to the pool.
     *
     * @param block The block to execute.
     * @return The result of the block.
     */
    fun <T> use(block: (RacoonManager) -> T): T {
        val blockResult = block(this)
        release()
        return blockResult
    }

    fun release() {
        RacoonDen.releaseManager(this)
    }

    internal fun prepare(query: String): PreparedStatement {
        return connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
    }

    /**
     * Creates a [QueryRacoon] of type [RacoonType.QUERY] with the given query.
     *
     * @param query The query to execute.
     * @return A [QueryRacoon] capable of handling the query and its results.
     */
    fun createQueryRacoon(@Language("mysql") query: String): QueryRacoon {
        return QueryRacoon(this, query)
    }

    internal companion object Factory {
        fun fromSettings(connectionSettings: ConnectionSettings): RacoonManager {
            return RacoonManager(DriverManager.getConnection(connectionSettings.toString()))
        }
    }
}