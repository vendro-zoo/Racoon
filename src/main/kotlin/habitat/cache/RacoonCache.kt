package habitat.cache

import habitat.definition.Table
import kotlin.reflect.KClass

class RacoonCache {
    private val cache: MutableMap<KClass<out Table>, MutableMap<Int, Table?>> = mutableMapOf()

    inline fun <reified T : Table> get(id: Int) = getK(id, T::class)

    @Suppress("UNCHECKED_CAST")
    fun <T : Table> getK(id: Int, kClass: KClass<T>): T? {
        val tableCache = cache[kClass] ?: mutableMapOf<Int, Table?>().apply { cache[kClass] = this }

        return tableCache[id]?.let { it as T }
    }

    inline fun <reified T : Table> put(table: T) = putK(table, T::class)

    fun <T : Table> putK(table: T, kClass: KClass<T>) {
        val tableCache = cache[kClass] ?: mutableMapOf<Int, Table?>().apply { cache[kClass] = this }

        tableCache[table.id ?: throw IllegalArgumentException("Cannot cache entity without id")] = table
    }

    inline fun <reified T : Table> remove(id: Int) = removeK(id, T::class)

    fun <T : Table> removeK(id: Int, kClass: KClass<T>) {
        val tableCache = cache[kClass] ?: return
        tableCache.remove(id)
    }

    fun clean() {
        cache.clear()
    }
}