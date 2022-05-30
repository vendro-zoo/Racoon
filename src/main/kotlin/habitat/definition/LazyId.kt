package habitat.definition

import habitat.RacoonManager
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

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

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return get()
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: LazyId<T>) {
        this.id = value.id
        this.manager = value.manager
        this.value = value.value
        this.isLoaded = value.isLoaded
    }

    constructor(id: Int, manager: RacoonManager, type: KClass<T>) : this(type, id, manager, null, false)
    constructor(value: T, type: KClass<T>) : this(type, value.id, null, value, true)
    constructor(type: KClass<T>) : this(type, -1, null, null, true)

    companion object {
        inline fun <reified T : Table> lazy(id: Int, manager: RacoonManager) = LazyId(id, manager, T::class)
        fun <T : Table> lazy(id: Int, manager: RacoonManager, type: KClass<T>) = LazyId(id, manager, type)

        inline fun <reified T : Table> defined(value: T) = LazyId(value, T::class)
        fun <T : Table> defined(value: T, type: KClass<T>) = LazyId(value, type)

        inline fun <reified T: Table> empty() = LazyId(T::class)
        fun <T: Table> empty(type: KClass<T>) = LazyId(type)
    }
}