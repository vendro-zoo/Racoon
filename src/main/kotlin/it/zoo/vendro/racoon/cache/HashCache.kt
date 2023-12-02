package it.zoo.vendro.racoon.cache

import it.zoo.vendro.racoon.definition.ColumnIgnore
import it.zoo.vendro.racoon.definition.IgnoreTarget
import it.zoo.vendro.racoon.definition.Table
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

class HashCache {
    internal val cache: MutableMap<KClass<Table<*, *>>, MutableMap<Any, Int>> = mutableMapOf()

    fun <I : Any, T : Table<I, *>> put(table: T) {
        val id = table.id ?: return
        @Suppress("UNCHECKED_CAST") val tableClass = table::class as KClass<Table<*, *>>
        val hash = calculateHash(table)
        val tableCache = cache.getOrPut(tableClass) { mutableMapOf() }
        tableCache[id] = hash
    }

    fun <I : Any, T : Table<I, *>> get(table: T): Int? {
        val id = table.id ?: return null
        @Suppress("UNCHECKED_CAST") val tableClass = table::class as KClass<Table<*, *>>
        val tableCache = cache[tableClass] ?: return null
        return tableCache[id]
    }

    fun <I : Any, T : Table<I, *>> compare(table: T): Boolean {
        val oldHash = get(table) ?: return true
        return calculateHash(table) == oldHash
    }

    fun calculateHash(table: Table<*, *>): Int {
        @Suppress("UNCHECKED_CAST") val tableClass = table::class as KClass<Table<*, *>>
        val properties = tableClass.memberProperties
        val values = properties
            .filter { field ->
                ColumnIgnore.shouldIgnore(field, IgnoreTarget.UPDATE)
            }
            .map { field ->
                field.get(table)
            }
        return Objects.hashCode(values.toTypedArray())
    }
}