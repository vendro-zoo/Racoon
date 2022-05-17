package habitat

import commons.configuration.ConnectionSettings
import commons.model.generateInsertQuery
import commons.model.generateSelectQuery
import commons.model.generateUpdateQuery
import commons.model.getValue
import habitat.racoons.ExecuteRacoon
import habitat.racoons.InsertRacoon
import habitat.racoons.QueryRacoon
import org.intellij.lang.annotations.Language
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

@Suppress("unused")
class RacoonManager(
    private val connection: Connection,
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

    inline fun <reified T : Any> find(id: Int): T? = find(id, T::class)

    fun <T : Any> find(id: Int, tClass: KClass<T>): T? {
        val queryRacoon = createQueryRacoon(generateSelectQuery(tClass))
        queryRacoon.setParam("id", id)
        return queryRacoon.mapToClass(tClass).firstOrNull()
    }

    inline fun <reified T : Any> insert(obj: T) = insert(obj, T::class)

    fun <T : Any> insert(obj: T, tClass: KClass<T>) = apply {
        val insertRacoon = createInsertRacoon(generateInsertQuery(tClass))
        val parameters = tClass.memberProperties
        for (field in parameters) insertRacoon.setParam(field.name, field.get(obj))
        insertRacoon.execute()

        obj::class.memberProperties.find { it.name == "id" }?.let {
            if (it is KMutableProperty<*>) it.setter.call(obj, getLastId())
        }
    }

    inline fun <reified T : Any> update(obj: T) = update(obj, T::class)

    fun <T : Any> update(obj: T, tClass: KClass<T>) = apply {
        val executeRacoon = createExecuteRacoon(generateUpdateQuery(tClass))
        val parameters = tClass.memberProperties
        for (field in parameters) executeRacoon.setParam(field.name, field.get(obj))
        executeRacoon.execute()

        getValue(obj, "id", tClass)?.let {
            if (it !is Int) throw IllegalArgumentException("id must be an Int")
            val updated = find(it, tClass) ?: throw SQLException("Could not find object with id '$it' " +
                    "while updating the fields")

            for (parameter in parameters) {
                if (parameter !is KMutableProperty<*>) continue
                parameter.setter.call(obj, getValue(updated, parameter.name, tClass))
            }
        } ?: throw IllegalArgumentException("id property not found in '${tClass.simpleName}'")
    }

    /**
     * Creates a [QueryRacoon] of type [RacoonType.QUERY] with the given query.
     *
     * @param query The query to execute.
     * @return A [QueryRacoon] capable of handling the query and its results.
     */
    fun createQueryRacoon(@Language("mysql") query: String): QueryRacoon = QueryRacoon(this, query)

    fun createInsertRacoon(@Language("mysql") query: String): InsertRacoon = InsertRacoon(this, query)

    fun createExecuteRacoon(@Language("mysql") query: String): ExecuteRacoon = ExecuteRacoon(this, query)

    internal companion object {
        fun fromSettings(connectionSettings: ConnectionSettings): RacoonManager {
            return RacoonManager(DriverManager.getConnection(connectionSettings.toString()))
        }
    }
}