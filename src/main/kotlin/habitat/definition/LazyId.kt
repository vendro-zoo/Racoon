package habitat.definition

import habitat.RacoonManager
import kotlin.reflect.KClass

class LazyId<out T: Table> private constructor(
    private val type: KClass<T>,
    val id: Int,

    private val manager: RacoonManager? = null,

    private var value: T? = null,
    private var isLoaded: Boolean = false
) {
    fun get(): T? {
        if (!isLoaded) {
            manager ?: throw IllegalArgumentException("No manager available")
            value = manager.findK(id, type)
            isLoaded = true
        }
        return value
    }

    companion object {
        inline fun <reified T : Table> lazy(id: Int, manager: RacoonManager) = lazy(id, manager, T::class)
        fun <T : Table> lazy(
            id: Int,
            manager: RacoonManager,
            type: KClass<T>
        ) = LazyId(
            type = type,
            id = id,
            manager = manager,
            isLoaded = false
        )

        inline fun <reified T : Table> defined(value: T) =
            defined(
                value,
                T::class,
                value.id ?: throw IllegalArgumentException("Cannot create a defined LazyId from a value without an id")
            )
        fun <T : Table> defined(
            value: T,
            type: KClass<T>,
            id: Int
        ) = LazyId(
            type = type,
            value = value,
            id = id,
            isLoaded = true
        )
    }
}