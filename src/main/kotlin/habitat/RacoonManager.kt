package habitat

import commons.configuration.ConnectionSettings
import commons.model.generateInsertQuery
import habitat.racoons.InsertRacoon
import habitat.racoons.QueryRacoon
import org.intellij.lang.annotations.Language
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

@Suppress("unused")
class RacoonManager(
    private val connection: Connection
) {
    var finalOpExecuted = false

    init {
        connection.autoCommit = false
    }

    /**
     * Closes the connection to the database.
     * This method should be called when the [RacoonManager] is no longer needed.
     */
    internal fun close() {
        connection.close()
    }

    fun commit() = apply {
        finalOpExecuted = true
        connection.commit()
    }

    fun rollback() = apply {
        finalOpExecuted = true
        connection.rollback()
    }

    /**
     * Executes the given block, commits if no operation has been specified and then releases the manager to the pool.
     *
     * @param block The block to execute.
     * @return The result of the block.
     */
    inline fun <T> use(block: (RacoonManager) -> T): T {
        val blockResult = block(this)
        if (!finalOpExecuted) commit()
        release()
        return blockResult
    }

    fun release() {
        RacoonDen.releaseManager(this)
    }

    internal fun prepare(query: String): PreparedStatement {
        return connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
    }

    fun getLastId(): Int {
        val statement = prepare("SELECT LAST_INSERT_ID()")
        val result = statement.executeQuery()
        result.next()
        return result.getInt(1)
    }

    inline fun <reified T: Any> insert(obj: T) = insert(obj, T::class)

    fun <T: Any> insert(obj: T, tClass: KClass<T>) = apply {
        val insertRacoon = createInsertRacoon(generateInsertQuery(tClass))
        for ((i, field) in tClass.memberProperties.withIndex()) insertRacoon.setParam(i + 1, field.get(obj))
        insertRacoon.execute()

        println(obj::class.memberProperties.find { it.name == "id" })

        obj::class.memberProperties.find { it.name == "id" }?.let {
            if (it is KMutableProperty<*>) it.setter.call(obj, getLastId())
        }
    }

    /**
     * Creates a [QueryRacoon] of type [RacoonType.QUERY] with the given query.
     *
     * @param query The query to execute.
     * @return A [QueryRacoon] capable of handling the query and its results.
     */
    fun createQueryRacoon(@Language("mysql") query: String): QueryRacoon = QueryRacoon(this, query)

    fun createInsertRacoon(@Language("mysql") query: String): InsertRacoon = InsertRacoon(this, query)

    internal companion object {
        fun fromSettings(connectionSettings: ConnectionSettings): RacoonManager {
            return RacoonManager(DriverManager.getConnection(connectionSettings.toString()))
        }
    }
}