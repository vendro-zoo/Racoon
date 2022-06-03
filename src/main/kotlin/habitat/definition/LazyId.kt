package habitat.definition

import habitat.RacoonManager
import kotlin.reflect.KClass

@Suppress("unused")
class LazyId<T: Table> private constructor(
    val type: KClass<T>,
    var id: Int?,

    var manager: RacoonManager? = null,

    var value: T? = null,
    var isLoaded: Boolean = false
) {
    fun get(): T? {
        if (!isLoaded) {
            val manager = manager ?: throw IllegalArgumentException("No manager available")
            val id = id ?: throw IllegalArgumentException("No id available")
            value = manager.findK(id, type)
            isLoaded = true
        }
        return value
    }

    companion object {
        inline fun <reified T : Table> lazy(id: Int, manager: RacoonManager) = lazy(id, manager, T::class)
        fun <T : Table> lazy(id: Int, manager: RacoonManager, type: KClass<T>) = LazyId(type, id, manager, null, false)

        inline fun <reified T : Table> defined(value: T) = defined(value, T::class)
        fun <T : Table> defined(value: T, type: KClass<T>) = LazyId(type, value.id, null, value, true)

        inline fun <reified T: Table> empty() = empty(T::class)
        fun <T: Table> empty(type: KClass<T>) = LazyId(type, null, null, null, true)
    }
}