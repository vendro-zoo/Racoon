package it.zoo.vendro.racoon.statements

import it.zoo.vendro.racoon.connection.ConnectionManager
import it.zoo.vendro.racoon.statements.parameters.Parameters


abstract class BatchableStatement<R: BatchableStatement<R>>(
    manager: ConnectionManager,
    originalQuery: String
) : Statement<R>(manager, originalQuery) {
    private val batchParameters: MutableList<Parameters> = mutableListOf()

    abstract fun executeBatch(): R

    fun bindBatchParameters() {
        // Checking if prepared statement and the mappings are set
        val preparedStatement = preparedStatement ?:
        throw IllegalStateException("A prepared statement must be set before binding parameters.")

        val parameterMapping = parameterMapping ?:
        throw IllegalStateException("Parameter mappings must be set before binding parameters.")

        // Checking the bindings
        batchParameters.withIndex().forEach { (index, parameters) ->
            if (parameters.indexedParameters.size != parameterMapping.indexedParametersMappings.size)
                throw IllegalStateException("The number of indexed parameters of batch ${index + 1} " +
                        "must be equal to the number of parameter mappings.")

            if (parameters.namedParameters.size != parameterMapping.namedParametersMappings.size)
                throw IllegalStateException("The number of named parameters of batch ${index + 1} " +
                        "must be equal to the number of parameter mappings.")
        }

        // Binding parameters
        batchParameters.forEach {
            it.bind(preparedStatement, parameterMapping)
            preparedStatement.addBatch()
        }
    }

    fun addBatch(block: (Parameters) -> Unit): R {
        val param = Parameters(manager)
        batchParameters.add(param)
        block(param)
        return self()
    }

}
