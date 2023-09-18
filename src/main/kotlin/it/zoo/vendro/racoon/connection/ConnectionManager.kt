package it.zoo.vendro.racoon.connection

import it.zoo.vendro.racoon.cache.HashCache
import it.zoo.vendro.racoon.cache.TableCache
import it.zoo.vendro.racoon.configuration.ConnectionSettings
import it.zoo.vendro.racoon.configuration.RacoonConfiguration
import it.zoo.vendro.racoon.definition.*
import it.zoo.vendro.racoon.exceptions.ConnectionUnavailable
import it.zoo.vendro.racoon.internals.query.generateDeleteQueryK
import it.zoo.vendro.racoon.internals.query.generateInsertQueryK
import it.zoo.vendro.racoon.internals.query.generateSelectQueryK
import it.zoo.vendro.racoon.internals.query.generateUpdateQueryK
import it.zoo.vendro.racoon.internals.utils.getValueK
import it.zoo.vendro.racoon.internals.utils.retryUntilNotNull
import it.zoo.vendro.racoon.statements.ExecuteStatement
import it.zoo.vendro.racoon.statements.InsertStatement
import it.zoo.vendro.racoon.statements.QueryStatement
import org.intellij.lang.annotations.Language
import java.io.FileNotFoundException
import java.sql.*
import kotlin.reflect.*
import kotlin.reflect.full.memberProperties

@Suppress("unused")
class ConnectionManager(
    internal val connection: Connection,
    internal val pool: ConnectionPool,
) {
    val config: RacoonConfiguration
        get() = pool.configuration

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

    internal val cache = TableCache(this)
    internal val hashes = HashCache()

    /**
     * Closes the connection to the database.
     * This method should be called when the [ConnectionManager] is no longer needed.
     *
     * @throws SQLException if the connection is already closed.
     */
    internal fun close() {
        if (closed || connection.isClosed) throw ConnectionUnavailable("The connection is already closed")
        connection.close()
        closed = true
    }

    /**
     * Commits the operations performed by the [ConnectionManager] to the database.
     *
     * @throws IllegalStateException if the [ConnectionManager] has already been committed or rolled back.
     * @throws SQLException if the connection is closed.
     */
    fun commit() = apply {
        if (closed || connection.isClosed) throw ConnectionUnavailable()
        if (finalOpExecuted) throw IllegalStateException("Can't commit after final operation has been executed")
        finalOpExecuted = true
        connection.commit()
    }

    /**
     * Rolls back the operations performed by the [ConnectionManager] to the database.
     *
     * @throws IllegalStateException if the [ConnectionManager] has already been committed or rolled back.
     * @throws SQLException if the connection is closed.
     */
    fun rollback() = apply {
        if (closed || connection.isClosed) throw ConnectionUnavailable()
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
    inline fun <T> use(block: (ConnectionManager) -> T): T {
        if (closed) throw ConnectionUnavailable()
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
     * @throws IllegalStateException if the [ConnectionManager] has already been committed or rolled back.
     * @throws SQLException if the connection is closed.
     */
    fun release() {
        if (closed || connection.isClosed) throw ConnectionUnavailable()
        if (!finalOpExecuted) throw IllegalStateException("Can't release before final operation has been executed")
        this.closed = true
        this.cache.clean()
        pool.releaseManager(this)
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
    inline fun <reified I : Any, reified T : Table<I, *>> findUncached(id: I): T? =
        findUncachedK(id, T::class, typeOf<I>())

    /**
     * Finds a record in the database and maps the result to the given class.
     *
     * @param id The id of the record to find.
     * @param tClass The class of the record to find.
     * @return The record mapped to the type [T].
     */
    fun <I : Any, T : Table<I, *>> findUncachedK(id: I, tClass: KClass<T>, iType: KType): T? {
        createQuery(generateSelectQueryK(tClass, config)).use { queryRacoon ->
            return queryRacoon.setParamK("id", id, iType)
                .uncheckedConsumeRows { row ->
                    row.mapToClassK(tClass)
                }!!.firstOrNull()?.also { hashes.put(it) }
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
    inline fun <reified I : Any, reified T : Table<I, *>> find(id: I): T? = findK(id, T::class, typeOf<I>())

    /**
     * Finds a record in the database and maps the result to the given class.
     *
     * If the record has already been found by calling this method, it will be returned from the cache.
     * If the record has not been found by previous calls, the query will be executed again and the result will be cached.
     *
     * @param id The id of the record to find.
     * @param kClass The class of the record to find.
     * @param iType The type of the id.
     * @return The record mapped to the type [T].
     * @see findUncachedK
     */
    fun <I : Any, T : Table<I, *>> findK(id: I, kClass: KClass<T>, iType: KType): T? {
        cache.getK(id, kClass)?.let { return it }

        val found = findUncachedK(id, kClass, iType)
        if (found != null) cache.putK(found, kClass)
        return found
    }

    /**
     * Behaves like [insertUncachedK], but instead of passing the class as a normal parameter, it is passed as a reified type.
     *
     * @param T The type that is being inserted.
     * @param obj The object to insert.
     * @return The [ConnectionManager] instance.
     */
    inline fun <reified I : Any, reified T : Table<I, *>> insertUncached(obj: T) =
        insertUncachedK(obj, T::class, typeOf<I>())

    private fun <I: Any, T : Table<I, *>> recursiveInsert(field: KProperty1<T, *>, obj: T) {
        if (field.returnType.classifier == LazyId::class) {
            val lazyId = (field.get(obj) as LazyId<*, *>?) ?: return
            val lazyIdValue = (lazyId.value ?: return) as Table<Any, *>
            if (lazyIdValue.id == null) insertK(lazyIdValue, lazyIdValue::class as KClass<Table<Any, *>>, lazyId.idType)
        }
    }

    /**
     * Inserts an object into the database and updates the id.
     *
     * @param obj The object to insert.
     * @param kClass The class of the object to insert.
     * @return The object with the id updated. Any old reference to the object will still be valid.
     */
    fun <I : Any, T : Table<I, *>> insertUncachedK(obj: T, kClass: KClass<T>, iType: KType) = obj.apply {
        val parameters = kClass.memberProperties

        createInsert(generateInsertQueryK(kClass, config)).use { insertRacoon ->
            for (field in parameters) {
                if (field.name == "id") continue
                if (ColumnIgnore.shouldIgnore(field, IgnoreTarget.INSERT)) continue
                recursiveInsert(field, obj)
                recursiveUpdate(field, obj)
                insertRacoon.setParamK(
                    ColumnName.getName(field, config),
                    field.get(obj),
                    field.returnType,
                    ColumnSetType.getInsertionMethod(field)
                )
            }
            insertRacoon.execute()
            obj.id = insertRacoon.generatedKeys[0] as I

            refreshK(obj, kClass, iType)
        }
    }

    /**
     * Behaves like [insertK], but instead of passing the class as a normal parameter, it is passed as a reified type.
     *
     * @param T The type that is being inserted.
     * @param obj The object to insert.
     */
    inline fun <reified I : Any, reified T : Table<I, *>> insert(obj: T) = insertK(obj, T::class, typeOf<I>())

    /**
     * Inserts an object into the database and updates the id.
     *
     * The inserted object is also inserted into the cache.
     *
     * @param obj The object to insert.
     * @param kClass The class of the object to insert.
     */
    fun <I : Any, T : Table<I, *>> insertK(obj: T, kClass: KClass<T>, iType: KType) =
        insertUncachedK(obj, kClass, iType).also { cache.putK(obj, kClass) }

    /**
     * Behaves like [updateK], but instead of passing the class as a normal parameter, it is passed as a reified type.
     *
     * @param T The type that is being updated.
     * @param obj The object to update.
     * @return The [ConnectionManager] instance.
     */
    inline fun <reified I : Any, reified T : Table<I, *>> updateUncached(obj: T) =
        updateUncachedK(obj, T::class, typeOf<I>())

    private fun <I: Any, T : Table<I, *>> recursiveUpdate(field: KProperty1<T, *>, obj: T) {
        if (field.returnType.classifier == LazyId::class) {
            val lazyId = (field.get(obj) as LazyId<*, *>?) ?: return
            val lazyIdValue = (lazyId.value ?: return) as Table<Any, *>
            if (lazyIdValue.id == null && !hashes.compare(lazyIdValue)) updateK(lazyIdValue, lazyIdValue::class as KClass<Table<Any, *>>, lazyId.idType)
        }
    }

    /**
     * Updates a record in the database with the given object.
     *
     * After executing the `update` statement, a query is executed,
     * and all the mutable properties are updated with the values returned by the query.
     *
     * @param obj The object to update.
     * @param kClass The class of the object to update.
     * @return The [ConnectionManager] instance.
     */
    fun <I : Any, T : Table<I, *>> updateUncachedK(obj: T, kClass: KClass<T>, iType: KType) = obj.apply {
        val parameters = kClass.memberProperties

        createExecute(generateUpdateQueryK(kClass, config)).use { executeRacoon ->
            for (field in parameters) {
                if (ColumnIgnore.shouldIgnore(field, IgnoreTarget.UPDATE)) continue
                recursiveInsert(field, obj)
                recursiveUpdate(field, obj)
                executeRacoon.setParamK(
                    ColumnName.getName(field, config),
                    field.get(obj),
                    field.returnType,
                    ColumnSetType.getInsertionMethod(field)
                )
            }
            executeRacoon.execute()

            refreshK(obj, kClass, iType)
        }
    }

    /**
     * Behaves like [updateK], but instead of passing the class as a normal parameter, it is passed as a reified type.
     *
     * @param T The type that is being updated.
     * @param obj The object to update.
     */
    inline fun <reified I : Any, reified T : Table<I, *>> update(obj: T) = updateK(obj, T::class, typeOf<I>())

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
    fun <I : Any, T : Table<I, *>> updateK(obj: T, kClass: KClass<T>, iType: KType) {
        updateUncachedK(obj, kClass, iType)
        cache.putK(obj, kClass)
    }

    /**
     * Behaves like [deleteK], but instead of passing the class as a normal parameter, it is passed as a reified type.
     *
     * @param T The type that is being deleted.
     * @param obj The object to delete.
     * @return The [ConnectionManager] instance.
     */
    inline fun <reified I : Any, reified T : Table<I, *>> deleteUncached(obj: T) =
        deleteUncachedK(obj, T::class, typeOf<I>())

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
     * @return The [ConnectionManager] instance.
     * @throws IllegalArgumentException if the object has no property with the name 'id'.
     */
    fun <I : Any, T : Table<I, *>> deleteUncachedK(obj: T, kClass: KClass<T>, iType: KType) = apply {
        val id = obj.id ?: throw IllegalArgumentException("Can't delete object without id")

        createExecute(generateDeleteQueryK(kClass, config)).use { executeRacoon ->
            executeRacoon.setParamK("id", id, iType).execute()
        }
    }

    /**
     * Behaves like [deleteK], but instead of passing the class as a normal parameter, it is passed as a reified type.
     *
     * @param T The type that is being deleted.
     * @param obj The object to delete.
     */
    inline fun <reified I : Any, reified T : Table<I, *>> delete(obj: T) = deleteK(obj, T::class, typeOf<I>())

    /**
     * Deletes a record from the database with the given object.
     *
     * The deleted object is also deleted from the cache.
     *
     * @param obj The object to delete.
     * @param kClass The class of the object to delete.
     */
    fun <I : Any, T : Table<I, *>> deleteK(obj: T, kClass: KClass<T>, iType: KType) {
        deleteUncachedK(obj, kClass, iType)
        cache.removeK(obj.id!!, kClass)
    }

    inline fun <reified I : Any, reified T : Table<I, *>> refresh(obj: T) = refreshK(obj, T::class, typeOf<I>())

    fun <I : Any, T : Table<I, *>> refreshK(obj: T, kClass: KClass<T>, iType: KType) = obj.apply {
        val id = this.id ?: throw IllegalArgumentException("Can't refresh object without id")
        val updated = findUncachedK(id, kClass, iType) ?: throw SQLException(
            "Could not find object with id '$id' " +
                    "while refreshing the fields"
        )

        for (parameter in kClass.memberProperties) {
            if (parameter !is KMutableProperty<*>) continue
            if (ColumnIgnore.shouldIgnore(parameter, IgnoreTarget.SELECT)) continue
            parameter.setter.call(this, getValueK(updated, parameter.name, kClass))
        }
    }

    /**
     * Creates a [QueryStatement] with the given query.
     *
     * @param query The query to execute.
     * @return A [QueryStatement] capable of handling the query and its results.
     */
    fun createQuery(@Language("mysql") query: String): QueryStatement = QueryStatement(this, query)

    /**
     * Creates a [QueryStatement] with the given sql file.
     *
     * @param fileName The name of the file to execute.
     * @return A [QueryStatement] capable of handling the query and its results.
     */
    fun importQuery(fileName: String): QueryStatement = createQuery(readSQLResourceFile(fileName))

    /**
     * Creates an [InsertStatement] with the given query.
     *
     * @param query The query to execute.
     * @return An [InsertStatement] capable of handling the query and its results.
     */
    fun createInsert(@Language("mysql") query: String): InsertStatement = InsertStatement(this, query)

    /**
     * Creates an [InsertStatement] with the given sql file.
     *
     * @param fileName The name of the file to execute.
     * @return An [InsertStatement] capable of handling the query and its results.
     */
    fun importInsert(fileName: String): InsertStatement = createInsert(readSQLResourceFile(fileName))

    /**
     * Creates an [ExecuteStatement] with the given query.
     *
     * @param query The query to execute.
     * @return An [ExecuteStatement] capable of handling the query.
     */

    fun createExecute(@Language("mysql") query: String): ExecuteStatement = ExecuteStatement(this, query)

    /**
     * Creates an [ExecuteStatement] with the given sql file.
     *
     * @param fileName The name of the file to execute.
     * @return An [ExecuteStatement] capable of handling the query.
     */
    fun importExecute(fileName: String): ExecuteStatement = createExecute(readSQLResourceFile(fileName))

    private fun readSQLResourceFile(fileName: String): String {
        val filePath = "/${config.resourcing.baseSQLPath}/${fileName}"
        return ConnectionManager::class.java.getResource(filePath)?.readText()
            ?: throw FileNotFoundException(filePath)
    }

    internal companion object {
        /**
         * Creates a [ConnectionManager] instance with the given [ConnectionSettings].
         *
         * @param connectionSettings The connection settings to use.
         * @return A [ConnectionManager] instance.
         */
        internal fun fromSettings(connectionSettings: ConnectionSettings, pool: ConnectionPool): ConnectionManager {
            val cm = ConnectionManager(
                retryUntilNotNull { DriverManager.getConnection(connectionSettings.toString()) },
                pool
            )
            val idleTimeout = pool.configuration.connection.idleTimeout

            cm.connection.autoCommit = false

            if (idleTimeout > 0) {
                cm.createExecute("SET wait_timeout = $idleTimeout, interactive_timeout = $idleTimeout")
                    .execute()
            }

            return cm
        }
    }
}