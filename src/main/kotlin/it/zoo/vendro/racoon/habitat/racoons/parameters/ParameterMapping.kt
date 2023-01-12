package it.zoo.vendro.racoon.habitat.racoons.parameters

import java.sql.SQLException

class ParameterMapping {
    val indexedParametersMappings: MutableMap<Int, Int> = mutableMapOf()
    val namedParametersMappings: MutableMap<String, Int> = mutableMapOf()

    fun addIndexed(originalIndex: Int, processedIndex: Int) {
        indexedParametersMappings[originalIndex] = processedIndex
    }

    fun addNamed(originalName: String, processedIndex: Int) {
        namedParametersMappings[originalName] = processedIndex
    }

    fun getIndexed(originalIndex: Int): Int {
        return indexedParametersMappings[originalIndex]
            ?: throw SQLException("Indexed parameter $originalIndex not found")
    }

    fun getNamed(originalName: String): Int {
        return namedParametersMappings[originalName]
            ?: throw SQLException("Named parameter $originalName not found")
    }
}