package it.zoo.vendro.racoon.habitat

import it.zoo.vendro.racoon.habitat.cache.RacoonCache
import it.zoo.vendro.racoon.habitat.configuration.RacoonConfiguration
import it.zoo.vendro.racoon.habitat.definition.ColumnName
import it.zoo.vendro.racoon.habitat.definition.IgnoreColumn
import it.zoo.vendro.racoon.habitat.definition.IgnoreTarget
import it.zoo.vendro.racoon.habitat.definition.Table
import it.zoo.vendro.racoon.habitat.racoons.ExecuteRacoon
import it.zoo.vendro.racoon.habitat.racoons.InsertRacoon
import it.zoo.vendro.racoon.habitat.racoons.QueryRacoon
import it.zoo.vendro.racoon.internals.configuration.ConnectionSettings
import it.zoo.vendro.racoon.internals.exceptions.ConnectionUnavailable
import it.zoo.vendro.racoon.internals.exceptions.connectionClosedException
import it.zoo.vendro.racoon.internals.model.getValueK
import it.zoo.vendro.racoon.internals.query.generateDeleteQueryK
import it.zoo.vendro.racoon.internals.query.generateInsertQueryK
import it.zoo.vendro.racoon.internals.query.generateSelectQueryK
import it.zoo.vendro.racoon.internals.query.generateUpdateQueryK
import it.zoo.vendro.racoon.internals.utils.retryUntilNotNull
import org.intellij.lang.annotations.Language
import java.io.FileNotFoundException
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

    internal val cache = RacoonCache()

    /**
     * Closes the connection to the database.
     * This method should be called when the [RacoonManager] is no longer needed.
     *
     * @throws SQLException if the connection is already closed.
     */
    internal fun close() {
        if (closed || connection.isClosed) throw ConnectionUnavailable("The connection is already closed")
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
        if (closed || connection.isClosed) throw connectionClosedException()
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
        if (closed || connection.isClosed) throw connectionClosedException()
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
        val blockResult = runCatching { block(this) }.fold(
            {
                if (!finalOpExecuted) commit()
                release()
                it
            },
            {
                try {
                    rollback()
                } catch (e: ConnectionUnavailable) {
                    throw ConnectionUnavailable(it)
                }
                release()
                throw it
            })
        return blockResult
    }

    /**
     * Releases the manager to the pool.
     *
     * @throws IllegalStateException if the [RacoonManager] has already been committed or rolled back.
     * @throws SQLException if the connection is closed.
     */
    fun release() {
        if (closed || connection.isClosed) throw connectionClosedException()
        if (!finalOpExecuted) throw IllegalStateException("Can't release before final operation has been executed")
        this.closed = true
        this.cache.clean()
        RacoonDen.releaseManager(this)
    }

    /**
     * Creates a prepared statement for the given query, where the [ResultSet] can be scrolled multiple times.
     *
     * @param query The query to execute.
     * @return The prepared statement.
     */
    internal fun prepareScrollable(query: String): PreparedStatement {
        return connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
    }

    /**
     * Creates a prepared statement for the given query, where the [ResultSet] contains the inserted keys.
     *
     * @param query The query to execute.
     * @return The prepared statement.
     */
    internal fun prepareInserted(query: String): PreparedStatement {
        return connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
    }

    /**
     * Creates a prepared statement for the given query.
     *
     * @param query The query to execute.
     * @return The prepared statement.
     */
    internal fun prepare(query: String): PreparedStatement {
        return connection.prepareStatement(query)
    }

    /**
     * Behaves like [findUncachedK], but instead of passing the class as a normal parameter, it is passed as a reified type.
     *
     * @param T The type to map the result to.
     * @param id The id of the record to find.
     * @return The record mapped to the type [T].
     * @see findUncachedK
     */
    inline fun <reified T : Table> findUncached(id: Int): T? = findUncachedK(id, T::class)

    /**
     * Finds a record in the database and maps the result to the given class.
     *
     * @param id The id of the record to find.
     * @param kClass The class of the record to find.
     * @return The record mapped to the type [T].
     */
    fun <T : Table> findUncachedK(id: Int, kClass: KClass<T>): T? {
        createQueryRacoon(generateSelectQueryK(kClass)).use { queryRacoon ->
            queryRacoon.setParam("id", id)
            return queryRacoon.mapToClassK(kClass).firstOrNull()
        }
    }

    /**
     * Behaves like [findK], but instead of passing the class as a normal parameter, it is passed as a reified type.
     *
     * @param T The type to map the result to.
     * @param id The id of the record to find.
     * @return The record mapped to the type [T].
     * @see findK
     */
    inline fun <reified T : Table> find(id: Int): T? = findK(id, T::class)

    /**
     * Finds a record in the database and maps the result to the given class.
     *
     * If the record has already been found by calling this method, it will be returned from the cache.
     * If the record has not been found by previous calls, the query will be executed again and the result will be cached.
     *
     * @param id The id of the record to find.
     * @param kClass The class of the record to find.
     * @return The record mapped to the type [T].
     * @see findUncachedK
     */
    fun <T : Table> findK(id: Int, kClass: KClass<T>): T? {
        cache.getK(id, kClass)?.let { return it }

        val found = findUncachedK(id, kClass)
        if (found != null) cache.putK(found, kClass)
        return found
    }

    /**
     * Behaves like [insertUncachedK], but instead of passing the class as a normal parameter, it is passed as a reified type.
     *
     * @param T The type that is being inserted.
     * @param obj The object to insert.
     * @return The [RacoonManager] instance.
     */
    inline fun <reified T : Table> insertUncached(obj: T) = insertUncachedK(obj, T::class)

    /**
     * Inserts an object into the database and updates the id.
     *
     * @param obj The object to insert.
     * @param kClass The class of the object to insert.
     * @return The object with the id updated. Any old reference to the object will still be valid.
     */
    fun <T : Table> insertUncachedK(obj: T, kClass: KClass<T>) = obj.apply {
        val parameters = kClass.memberProperties

        createInsertRacoon(generateInsertQueryK(kClass)).use { insertRacoon ->
            for (field in parameters) {
                if (IgnoreColumn.shouldIgnore(field, IgnoreTarget.INSERT)) continue
                insertRacoon.setParam(ColumnName.getName(field), field.get(obj))
            }
            insertRacoon.execute()
            obj.id = insertRacoon.generatedKeys[0]

            refreshK(obj, kClass)
        }
    }

    /**
     * Behaves like [insertK], but instead of passing the class as a normal parameter, it is passed as a reified type.
     *
     * @param T The type that is being inserted.
     * @param obj The object to insert.
     */
    inline fun <reified T : Table> insert(obj: T) = insertK(obj, T::class)

    /**
     * Inserts an object into the database and updates the id.
     *
     * The inserted object is also inserted into the cache.
     *
     * @param obj The object to insert.
     * @param kClass The class of the object to insert.
     */
    fun <T : Table> insertK(obj: T, kClass: KClass<T>) =
        insertUncachedK(obj, kClass).also { cache.putK(obj, kClass) }

    /**
     * Behaves like [updateK], but instead of passing the class as a normal parameter, it is passed as a reified type.
     *
     * @param T The type that is being updated.
     * @param obj The object to update.
     * @return The [RacoonManager] instance.
     */
    inline fun <reified T : Table> updateUncached(obj: T) = updateUncachedK(obj, T::class)

    /**
     * Updates a record in the database with the given object.
     *
     * After executing the `update` statement, a query is executed,
     * and all the mutable properties are updated with the values returned by the query.
     *
     * @param obj The object to update.
     * @param kClass The class of the object to update.
     * @return The [RacoonManager] instance.
     */
    fun <T : Table> updateUncachedK(obj: T, kClass: KClass<T>) = obj.apply {
        val parameters = kClass.memberProperties

        createExecuteRacoon(generateUpdateQueryK(kClass)).use { executeRacoon ->
            for (field in parameters) {
                if (IgnoreColumn.shouldIgnore(field, IgnoreTarget.UPDATE)) continue
                executeRacoon.setParam(ColumnName.getName(field), field.get(obj))
            }
            executeRacoon.execute()

            refreshK(obj, kClass)
        }
    }

    /**
     * Behaves like [updateK], but instead of passing the class as a normal parameter, it is passed as a reified type.
     *
     * @param T The type that is being updated.
     * @param obj The object to update.
     */
    inline fun <reified T : Table> update(obj: T) = updateK(obj, T::class)

    /**
     * Updates a record in the database with the given object.
     *
     * After executing the `update` statement, a query is executed,
     * and all the mutable properties are updated with the values returned by the query.
     *
     * The updated object is also updated in the cache.
     *
     * @param obj The object to update.
     * @param kClass The class of the object to update.
     */
    fun <T : Table> updateK(obj: T, kClass: KClass<T>) {
        updateUncachedK(obj, kClass)
        cache.putK(obj, kClass)
    }

    /**
     * Behaves like [deleteK], but instead of passing the class as a normal parameter, it is passed as a reified type.
     *
     * @param T The type that is being deleted.
     * @param obj The object to delete.
     * @return The [RacoonManager] instance.
     */
    inline fun <reified T : Table> deleteUncached(obj: T) = deleteUncachedK(obj, T::class)

    /**
     * Deletes a record from the database with the given object.
     *
     * NOTE: using this method while using [findK] is extremely not recommended.
     * Doing so will cause the cache to be outdated,
     * and may result in a find operation returning an object that is not in the database.
     * Use [deleteK] instead.
     *
     * @param obj The object to delete.
     * @param kClass The class of the object to delete.
     * @return The [RacoonManager] instance.
     * @throws IllegalArgumentException if the object has no property with the name 'id'.
     */
    fun <T : Table> deleteUncachedK(obj: T, kClass: KClass<T>) = apply {
        val id = obj.id ?: throw IllegalArgumentException("Can't delete object without id")

        createExecuteRacoon(generateDeleteQueryK(kClass)).use { executeRacoon ->
            executeRacoon.setParam("id", id).execute()
        }
    }

    /**
     * Behaves like [deleteK], but instead of passing the class as a normal parameter, it is passed as a reified type.
     *
     * @param T The type that is being deleted.
     * @param obj The object to delete.
     */
    inline fun <reified T : Table> delete(obj: T) = deleteK(obj, T::class)

    /**
     * Deletes a record from the database with the given object.
     *
     * The deleted object is also deleted from the cache.
     *
     * @param obj The object to delete.
     * @param kClass The class of the object to delete.
     */
    fun <T : Table> deleteK(obj: T, kClass: KClass<T>) {
        deleteUncachedK(obj, kClass)
        cache.removeK(obj.id!!, kClass)
    }

    inline fun <reified T : Table> refresh(obj: T) = refreshK(obj, T::class)

    fun <T : Table> refreshK(obj: T, kClass: KClass<T>) = obj.apply {
        val id = this.id ?: throw IllegalArgumentException("Can't refresh object without id")
        val updated = findUncachedK(id, kClass) ?: throw SQLException(
            "Could not find object with id '$id' " +
                    "while refreshing the fields"
        )

        for (parameter in kClass.memberProperties) {
            if (parameter !is KMutableProperty<*>) continue
            parameter.setter.call(this, getValueK(updated, parameter.name, kClass))
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
     * Creates a [QueryRacoon] with the given sql file.
     *
     * @param fileName The name of the file to execute.
     * @return A [QueryRacoon] capable of handling the query and its results.
     */
    fun importQueryRacoon(fileName: String): QueryRacoon = createQueryRacoon(readSQLResourceFile(fileName))

    /**
     * Creates an [InsertRacoon] with the given query.
     *
     * @param query The query to execute.
     * @return An [InsertRacoon] capable of handling the query and its results.
     */
    fun createInsertRacoon(@Language("mysql") query: String): InsertRacoon = InsertRacoon(this, query)

    /**
     * Creates an [InsertRacoon] with the given sql file.
     *
     * @param fileName The name of the file to execute.
     * @return An [InsertRacoon] capable of handling the query and its results.
     */
    fun importInsertRacoon(fileName: String): InsertRacoon = createInsertRacoon(readSQLResourceFile(fileName))

    /**
     * Creates an [ExecuteRacoon] with the given query.
     *
     * @param query The query to execute.
     * @return An [ExecuteRacoon] capable of handling the query.
     */

    fun createExecuteRacoon(@Language("mysql") query: String): ExecuteRacoon = ExecuteRacoon(this, query)

    /**
     * Creates an [ExecuteRacoon] with the given sql file.
     *
     * @param fileName The name of the file to execute.
     * @return An [ExecuteRacoon] capable of handling the query.
     */
    fun importExecuteRacoon(fileName: String): ExecuteRacoon = createExecuteRacoon(readSQLResourceFile(fileName))

    internal companion object {
        /**
         * Creates a [RacoonManager] instance with the given [ConnectionSettings].
         *
         * @param connectionSettings The connection settings to use.
         * @return A [RacoonManager] instance.
         */
        internal fun fromSettings(connectionSettings: ConnectionSettings): RacoonManager {
            val rm = RacoonManager(retryUntilNotNull { DriverManager.getConnection(connectionSettings.toString()) })
            val idleTimeout = RacoonConfiguration.Connection.connectionSettings.idleTimeout

            rm.connection.autoCommit = false

            if (idleTimeout > 0) {
                rm.createExecuteRacoon("SET wait_timeout = $idleTimeout, interactive_timeout = $idleTimeout")
                    .execute()
            }

            return rm
        }

        fun readSQLResourceFile(fileName: String): String {
            val filePath = "/${RacoonConfiguration.Resourcing.baseSQLPath}/${fileName}"
            return RacoonManager::class.java.getResource(filePath)?.readText()
                ?: throw FileNotFoundException(filePath)
        }
    }
}