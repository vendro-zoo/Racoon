package it.zoo.vendro.racoon.habitat.statements

import it.zoo.vendro.racoon.habitat.ConnectionManager
import it.zoo.vendro.racoon.habitat.configuration.RacoonConfiguration
import it.zoo.vendro.racoon.habitat.statements.result.QueryResult
import it.zoo.vendro.racoon.habitat.statements.result.QueryResultRow
import it.zoo.vendro.racoon.internals.query.QueryProcessing
import java.sql.ResultSet
import kotlin.reflect.KClass

@Suppress("unused")
class QueryStatement(
    manager: ConnectionManager,
    originalQuery: String,
) : Statement<QueryStatement>(manager, originalQuery) {
    internal val config: RacoonConfiguration
        get() = manager.pool.configuration
    internal val tableAliases: MutableMap<KClass<*>, String> = mutableMapOf()
    internal val queryResults = mutableListOf<QueryResult>()
    internal var executeResult: Boolean? = false
        private set
    internal var hasAnotherResult = true
        private set
    internal var isClosed = false
        private set

    /**
     * Adds a table alias to be used when mapping the result of the query to a class.
     *
     * This overrides any annotation and defaults mapping from the class to a table.
     *
     * If the query contains the same table multiple times,
     * the alias must be re-set before each mapping.
     *
     * @param clazz The class to alias.
     * @param alias The alias to use.
     */
    fun setAlias(clazz: KClass<*>, alias: String) = apply { tableAliases[clazz] = alias }

    /**
     * Processes the query and saves the result in the [QueryStatement].
     *
     * In more details, the query is first processed by binding the parameters, and then the query is executed.
     *
     * @return the [QueryStatement] itself
     */
    override fun execute() = apply {
        val queryProcessingResult = QueryProcessing.reconstructQuery(originalQuery, parameters, manager.config)

        val processedQuery = queryProcessingResult.first
        parameterMapping = queryProcessingResult.second

        val preparedStatement = manager.prepareScrollable(processedQuery)
            .also {
                this.preparedStatement = it
            }

        bindParameters()

        executeResult = preparedStatement.execute()
    }

    private fun getNextResultSet(): ResultSet? {
        val immutablePrepared = preparedStatement ?: execute().let { preparedStatement!! }
        var found = executeResult ?: immutablePrepared.moreResults
        executeResult = null

        while (true) {
            if (found) {
                return immutablePrepared.resultSet
            } else {
                if (immutablePrepared.updateCount == -1) return null
            }
            found = immutablePrepared.moreResults
        }
    }

    fun getNext(): QueryResult? {
        if (!hasAnotherResult) return null
        val rs = getNextResultSet() ?: run {
            hasAnotherResult = false
            return null
        }
        val result = QueryResult(this, rs)
        queryResults.add(result)
        return result
    }

    override fun close() {
        if (!isClosed) {
            isClosed = true
            queryResults.forEach { it.close() }
            super.close()
        }
    }

    // QueryResultShortcuts
    fun <T> uncheckedConsumeRows(processor: (QueryResultRow) -> T) = getNext()?.consumeRows(processor)
    fun <T> consumeRows(processor: (QueryResultRow) -> T) = (getNext()
        ?: throw IllegalStateException("No result set found")).consumeRows(processor)

    // QueryResultRowShortcuts
    fun <T : Any> mapToClassK(clazz: KClass<T>): List<T> = consumeRows { it.mapToClassK(clazz) }
    inline fun <reified T : Any> mapToClass(): List<T> = consumeRows { it.mapToClass() }
    inline fun <reified T : Any> multiMapToClass(): List<T> = consumeRows { it.multiMapToClass() }
    fun mapToNullableInt() = consumeRows { it.mapToNullableInt() }
    fun mapToInt() = consumeRows { it.mapToInt() }
    fun mapToNullableLong() = consumeRows { it.mapToNullableLong() }
    fun mapToLong() = consumeRows { it.mapToLong() }
    fun mapToNullableShort() = consumeRows { it.mapToNullableShort() }
    fun mapToShort() = consumeRows { it.mapToShort() }
    fun mapToNullableByte() = consumeRows { it.mapToNullableByte() }
    fun mapToByte() = consumeRows { it.mapToByte() }
    fun mapToNullableFloat() = consumeRows { it.mapToNullableFloat() }
    fun mapToFloat() = consumeRows { it.mapToFloat() }
    fun mapToNullableDouble() = consumeRows { it.mapToNullableDouble() }
    fun mapToDouble() = consumeRows { it.mapToDouble() }
    fun mapToNullableBigDecimal() = consumeRows { it.mapToNullableBigDecimal() }
    fun mapToBigDecimal() = consumeRows { it.mapToBigDecimal() }
    fun mapToNullableBoolean() = consumeRows { it.mapToNullableBoolean() }
    fun mapToBoolean() = consumeRows { it.mapToBoolean() }
    fun mapToNullableString() = consumeRows { it.mapToNullableString() }
    fun mapToString() = consumeRows { it.mapToString() }

}