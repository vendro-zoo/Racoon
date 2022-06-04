package habitat.definition

import habitat.RacoonManager
import kotlin.reflect.KClass

@Suppress("unused")
class LazyId<T: Table> private constructor(
    val type: KClass<T>,
    val id: Int?,

    val manager: RacoonManager? = null,

    var value: T?,
    var isLoaded: Boolean = false
) {
    fun get(): T {
        if (!isLoaded) {
            manager ?: throw IllegalArgumentException("No manager available")
            id ?: throw IllegalArgumentException("No id available")
            value = manager.findK(id, type)
            isLoaded = true
        }
        return value ?: throw IllegalArgumentException("No value found of type ${type.simpleName} with id $id")
    }

    companion object {
        inline fun <reified T : Table> lazy(id: Int, manager: RacoonManager) = lazy(id, manager, T::class)
        fun <T : Table> lazy(id: Int, manager: RacoonManager, type: KClass<T>) = LazyId(type, id, manager, null, false)

        inline fun <reified T : Table> defined(value: T) = defined(value, T::class)
        fun <T : Table> defined(value: T, type: KClass<T>) = LazyId(type, value.id, null, value, true)
    }
}