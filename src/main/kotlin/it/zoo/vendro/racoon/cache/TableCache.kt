package it.zoo.vendro.racoon.cache

import it.zoo.vendro.racoon.configuration.RacoonConfiguration
import it.zoo.vendro.racoon.connection.ConnectionManager
import it.zoo.vendro.racoon.definition.Table
import kotlin.math.min
import kotlin.reflect.KClass

class TableCache(
    val connectionManager: ConnectionManager
) {
    private val config: RacoonConfiguration
        get() = connectionManager.pool.configuration
    internal var cacheSize: Int = 0
    internal val cache: MutableMap<KClass<out Table<*>>, MutableMap<Any, Pair<Table<*>?, Long>>> = mutableMapOf()

    inline fun <I : Any, reified T : Table<Any>> get(id: I) = getK(id, T::class)

    @Suppress("UNCHECKED_CAST")
    fun <I : Any, T : Table<I>> getK(id: I, kClass: KClass<T>): T? {
        return cache[kClass]?.let { mainMap ->
            mainMap[id]?.let {
                val v = it.first as T
                mainMap[id] = v to System.currentTimeMillis()
                v
            }
        }
    }

    inline fun <I : Any, reified T : Table<I>> put(table: T) = putK(table, T::class)

    fun <I : Any, T : Table<I>> putK(table: T, kClass: KClass<T>) {
        val id = table.id ?: throw IllegalArgumentException("Cannot cache an entity without id")

        // Get the secondary map (might be null)
        var tableCache = cache[kClass]

        // Cleaning the cache if it's too big
        if (
            (tableCache == null || !tableCache.containsKey(id)) &&
            cacheSize >= config.caching.maxEntries
        ) forceCleanStale()

        // Checking if the cache is still too big
        if (cacheSize < config.caching.maxEntries) {
            // Creating the secondary map if it doesn't exist
            tableCache = tableCache ?: mutableMapOf<Any, Pair<Table<*>?, Long>>().apply { cache[kClass] = this }
            // Incrementing the cache size
            if (!tableCache.containsKey(id)) cacheSize++
            // Saving the entity in the cache
            tableCache[id] = table to System.currentTimeMillis()
        }
    }

    inline fun <I : Any, reified T : Table<I>> remove(id: I) = removeK(id, T::class)

    fun <I : Any, T : Table<I>> removeK(id: I, kClass: KClass<T>) {
        val tableCache = cache[kClass] ?: return
        tableCache.remove(id)
        cacheSize--
    }

    fun clean() {
        cache.clear()
    }

    fun forceCleanStale() {
        // Create the association (table, id, timestamp) of all entries in the cache
        cache.entries
            .fold(mutableListOf<Triple<KClass<out Table<*>>, Any, Long>>()) { acc, mainMap ->
                acc.apply {
                    addAll(mainMap.value.entries.map { (secondMapKey, secondMapValue) ->
                        Triple(mainMap.key, secondMapKey, secondMapValue.second)
                    })
                }
            }
            // Sort the entries by timestamp
            .sortedBy { it.third }
            // Get the first N entries (the oldest ones)
            .subList(0, min(config.caching.maxEntries, cacheSize))
            // Remove the entries from the cache
            .forEach {
                val f: KClass<Table<Any>> = it.first as KClass<Table<Any>>
                val s: Any = it.second
                removeK(s, f)
            }
    }
}