package habitat

import commons.configuration.ConnectionSettings
import commons.exceptions.connectionClosedException
import commons.query.generateInsertQueryK
import commons.query.generateSelectQueryK
import commons.query.generateUpdateQueryK
import commons.model.getValueK
import habitat.definition.Table
import habitat.racoons.ExecuteRacoon
import habitat.racoons.InsertRacoon
import habitat.racoons.QueryRacoon
import org.intellij.lang.annotations.Language
import java.sql.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

@Suppress("unused")
class RacoonManager(
    internal val connection: Connection,
) {
    /**
     * A state indicating whether a final operation such as commit or rollback has been performed.
     *
     * If false, a final operation has not been performed, otherwise it has.
     */
    var finalOpExecuted = false
        private set

    /**
     * A state indicating whether the manager can still be used or not.
     *
     * The manager is considered closed if it has been released to the pool or if the connection has been closed.
     */
    var closed = false
        internal set

    init {
        connection.autoCommit = false
    }

    /**
     * Closes the connection to the database.
     * This method should be called when the [RacoonManager] is no longer needed.
     *
     * @throws SQLException if the connection is already closed.
     */
    internal fun close() {
        if (closed) throw SQLException("Connection is already closed.")
        connection.close()
        closed = true
    }

    /**
     * Commits the operations performed by the [RacoonManager] to the database.
     *
     * @throws IllegalStateException if the [RacoonManager] has already been committed or rolled back.
     * @throws SQLException if the connection is closed.
     */
    fun commit() = apply {
        if (closed) throw connectionClosedException()
        if (finalOpExecuted) throw IllegalStateException("Can't commit after final operation has been executed")
        finalOpExecuted = true
        connection.commit()
    }

    /**
     * Rolls back the operations performed by the [RacoonManager] to the database.
     *
     * @throws IllegalStateException if the [RacoonManager] has already been committed or rolled back.
     * @throws SQLException if the connection is closed.
     */
    fun rollback() = apply {
        if (closed) throw connectionClosedException()
        if (finalOpExecuted) throw IllegalStateException("Can't rollback after final operation has been executed")
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
        if (closed) throw connectionClosedException()
        if (finalOpExecuted) throw IllegalStateException("Can't use after final operation has been executed")
        val blockResult = block(this)
        if (!finalOpExecuted) commit()
        release()
        return blockResult
    }

    /**
     * Releases the manager to the pool.
     *
     * @throws IllegalStateException if the [RacoonManager] has already been committed or rolled back.
     * @throws SQLException if the connection is closed.
     */
    fun release() {
        if (closed) throw connectionClosedException()
        if (!finalOpExecuted) throw IllegalStateException("Can't release before final operation has been executed")
        this.closed = true
        RacoonDen.releaseManager(this)
    }

    /**
     * Creates a prepared statement for the given query, where the [ResultSet] can be scrolled multiple times.
     *
     * @param query The query to execute.
     * @return The prepared statement.
     */
    internal fun prepare(query: String): PreparedStatement {
        return connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
    }

    /**
     * Gets the last inserted id.
     *
     * This is done by executing the query '`SELECT LAST_INSERT_ID()`'.
     *
     * @return The last inserted id.
     */
    fun getLastId(): Int {
        val statement = prepare("SELECT LAST_INSERT_ID()")
        val result = statement.executeQuery()
        result.next()
        return result.getInt(1)
    }

    /**
     * Behaves like [findK], but instead of passing the class as a normal parameter, it is passed as a reified type.
     *
     * @param T The type to map the result to.
     * @param id The id of the record to find.
     * @return The record mapped to the type [T].
     * @see findK
     */
    inline fun <reified T : Any> find(id: Int): T? = findK(id, T::class)

    /**
     * Finds a record in the database and maps the result to the given class.
     *
     * @param id The id of the record to find.
     * @param kClass The class of the record to find.
     * @return The record mapped to the type [T].
     */
    fun <T : Any> findK(id: Int, kClass: KClass<T>): T? {
        val queryRacoon = createQueryRacoon(generateSelectQueryK(kClass))
        queryRacoon.setParam("id", id)
        return queryRacoon.mapToClass(kClass).firstOrNull()
    }

    /**
     * Behaves like [insertK], but instead of passing the class as a normal parameter, it is passed as a reified type.
     *
     * @param T The type that is being inserted.
     * @param obj The object to insert.
     * @return The [RacoonManager] instance.
     */
    inline fun <reified T : Any> insert(obj: T) = insertK(obj, T::class)

    /**
     * Inserts an object into the database and updates the id.
     *
     * If the object has no property with the name 'id', no error is thrown, but the id is not updated.
     *
     * @param obj The object to insert.
     * @param kClass The class of the object to insert.
     * @return The [RacoonManager] instance.
     */
    fun <T : Any> insertK(obj: T, kClass: KClass<T>) = apply {
        val insertRacoon = createInsertRacoon(generateInsertQueryK(kClass))
        val parameters = kClass.memberProperties
        for (field in parameters) insertRacoon.setParam(field.name, field.get(obj))
        insertRacoon.execute()

        obj::class.memberProperties.find { it.name == "id" }?.let {
            if (it is KMutableProperty<*>) it.setter.call(obj, getLastId())
        }
    }

    /**
     * Behaves like [updateK], but instead of passing the class as a normal parameter, it is passed as a reified type.
     *
     * @param T The type that is being updated.
     * @param obj The object to update.
     * @return The [RacoonManager] instance.
     */
    inline fun <reified T : Table> update(obj: T) = updateK(obj, T::class)

    /**
     * Updates a record in the database with the given object.
     *
     * If the object has a property with the name 'id', then a query is executed,
     * and all the mutable properties are updated with the values of the record with the given id.
     *
     * @param obj The object to update.
     * @param kClass The class of the object to update.
     */
    fun <T : Table> updateK(obj: T, kClass: KClass<T>) = apply {
        val executeRacoon = createExecuteRacoon(generateUpdateQueryK(kClass))
        val parameters = kClass.memberProperties
        for (field in parameters) executeRacoon.setParam(field.name, field.get(obj))
        executeRacoon.execute()

        obj.id?.let {
            val updated = findK(it, kClass) ?: throw SQLException("Could not find object with id '$it' " +
                    "while updating the fields")

            for (parameter in parameters) {
                if (parameter !is KMutableProperty<*>) continue
                parameter.setter.call(obj, getValueK(updated, parameter.name, kClass))
            }
        }
    }

    /**
     * Creates a [QueryRacoon] with the given query.
     *
     * @param query The query to execute.
     * @return A [QueryRacoon] capable of handling the query and its results.
     */
    fun createQueryRacoon(@Language("mysql") query: String): QueryRacoon = QueryRacoon(this, query)

    /**
     * Creates an [InsertRacoon] with the given query.
     *
     * @param query The query to execute.
     * @return An [InsertRacoon] capable of handling the query and its results.
     */
    fun createInsertRacoon(@Language("mysql") query: String): InsertRacoon = InsertRacoon(this, query)

    /**
     * Creates an [ExecuteRacoon] with the given query.
     *
     * @param query The query to execute.
     * @return An [ExecuteRacoon] capable of handling the query.
     */
    fun createExecuteRacoon(@Language("mysql") query: String): ExecuteRacoon = ExecuteRacoon(this, query)

    internal companion object {
        /**
         * Creates a [RacoonManager] instance with the given [ConnectionSettings].
         *
         * @param connectionSettings The connection settings to use.
         * @return A [RacoonManager] instance.
         */
        internal fun fromSettings(connectionSettings: ConnectionSettings): RacoonManager {
            return RacoonManager(DriverManager.getConnection(connectionSettings.toString()))
        }
    }
}