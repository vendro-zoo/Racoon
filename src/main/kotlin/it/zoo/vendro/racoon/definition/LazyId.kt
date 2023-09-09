package it.zoo.vendro.racoon.definition

import it.zoo.vendro.racoon.connection.ConnectionManager
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@Suppress("unused")
/**
 * Allows to get a [Table] from another [Table]'s property
 *
 * The value of the linked table is lazily loaded from the database.
 * Once the value is loaded, it is cached in memory.
 *
 * @param T the type of the linked table
 */
class LazyId<T : Table<I>, I : Any> private constructor(
    /**
     * The [KClass] of the linked table.
     */
    val type: KClass<T>,
    val idType: KType,
    /**
     * The id of the record in the linked table.
     */
    val id: I?,

    /**
     * The [ConnectionManager] instance used to access the database.
     */
    val manager: ConnectionManager? = null,

    /**
     * The value of the linked table.
     */
    var value: T?,
    /**
     * A flag indicating whether the value needs to be loaded from the database.
     */
    var isLoaded: Boolean = false
) {
    /**
     * Gets the value of the linked table.
     *
     * If the value is not loaded yet, it is loaded from the database and cached in memory.
     *
     * @return the value of the linked table
     * @throws IllegalArgumentException if the value is not loaded yet and the table is not defined in the database
     */
    fun get(): T {
        if (!isLoaded) {
            // Checks that the state of the LazyId is valid
            manager ?: throw IllegalArgumentException("No manager available")
            id ?: throw IllegalArgumentException("No id available")

            // Get the value and sets the flag to "loaded"
            value = manager.findK(id, type, idType)
            isLoaded = true
        }
        // Returns the value or throws an exception if the value has not been found
        return value ?: throw IllegalArgumentException("No value found of type ${type.simpleName} with id $id")
    }

    companion object {
        /**
         * Behaves like [lazyK] but the type of the linked table is passed as a reified type parameter.
         *
         * @see [lazyK]
         */
        inline fun <reified I : Any, reified T : Table<Any>> lazy(id: I, manager: ConnectionManager) =
            lazyK(id, manager, T::class, typeOf<I>())

        /**
         * Creates a [LazyId] instance that still needs to be evaluated.
         *
         * @param id the id of the record in the linked table
         * @param manager the [ConnectionManager] instance used to access the database
         * @param type the [KClass] of the linked table
         * @return a [LazyId] instance that still needs to be evaluated
         */
        fun <I : Any, T : Table<I>> lazyK(id: I, manager: ConnectionManager, type: KClass<T>, iType: KType) =
            LazyId(type, iType, id, manager, null, false)

        /**
         * Behaves like [definedK] but the type of the linked table is passed as a reified type parameter.
         *
         * @see [definedK]
         */
        inline fun <reified I : Any, reified T : Table<I>> defined(value: T) = definedK(value, T::class, typeOf<I>())

        /**
         * Creates a [LazyId] instance that is already evaluated.
         *
         * @param value the value of the linked table
         * @param type the [KClass] of the linked table
         * @return a [LazyId] instance that is already evaluated
         */
        fun <I : Any, T : Table<I>> definedK(value: T, type: KClass<T>, iType: KType) =
            LazyId(type, iType, value.id, null, value, true)
    }
}