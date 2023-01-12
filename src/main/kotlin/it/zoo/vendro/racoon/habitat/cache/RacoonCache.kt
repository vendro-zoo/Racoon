package it.zoo.vendro.racoon.habitat.cache

import it.zoo.vendro.racoon.habitat.configuration.RacoonConfiguration
import it.zoo.vendro.racoon.habitat.definition.Table
import kotlin.math.min
import kotlin.reflect.KClass

class RacoonCache {
    internal var cacheSize: Int = 0
    internal val cache: MutableMap<KClass<out Table>, MutableMap<Int, Pair<Table?, Long>>> = mutableMapOf()

    inline fun <reified T : Table> get(id: Int) = getK(id, T::class)

    @Suppress("UNCHECKED_CAST")
    fun <T : Table> getK(id: Int, kClass: KClass<T>): T? {
        return cache[kClass]?.let { mainMap ->
            mainMap[id]?.let {
                val v = it.first as T
                mainMap[id] = v to System.currentTimeMillis()
                v
            }
        }
    }

    inline fun <reified T : Table> put(table: T) = putK(table, T::class)

    fun <T : Table> putK(table: T, kClass: KClass<T>) {
        val id = table.id ?: throw IllegalArgumentException("Cannot cache an entity without id")

        // Get the secondary map (might be null)
        var tableCache = cache[kClass]

        // Cleaning the cache if it's too big
        if (
            (tableCache == null || !tableCache.containsKey(id)) &&
            cacheSize >= RacoonConfiguration.Caching.maxEntries
        ) forceCleanStale()

        // Checking if the cache is still too big
        if (cacheSize < RacoonConfiguration.Caching.maxEntries) {
            // Creating the secondary map if it doesn't exist
            tableCache = tableCache ?: mutableMapOf<Int, Pair<Table?, Long>>().apply { cache[kClass] = this }
            // Incrementing the cache size
            if (!tableCache.containsKey(id)) cacheSize++
            // Saving the entity in the cache
            tableCache[id] = table to System.currentTimeMillis()
        }
    }

    inline fun <reified T : Table> remove(id: Int) = removeK(id, T::class)

    fun <T : Table> removeK(id: Int, kClass: KClass<T>) {
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
            .fold(mutableListOf<Triple<KClass<out Table>, Int, Long>>()) { acc, mainMap ->
                acc.apply {
                    addAll(mainMap.value.entries.map { (secondMapKey, secondMapValue) ->
                        Triple(mainMap.key, secondMapKey, secondMapValue.second)
                    })
                }
            }
            // Sort the entries by timestamp
            .sortedBy { it.third }
            // Get the first N entries (the oldest ones)
            .subList(0, min(RacoonConfiguration.Caching.maxEntries, cacheSize))
            // Remove the entries from the cache
            .forEach {
                removeK(it.second, it.first)
            }
    }
}