package it.zoo.vendro.racoon.definition

import it.zoo.vendro.racoon.connection.ConnectionManager
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * Indicates a class that defines a table in the database.
 */
interface Table<T : Any> {
    /**
     * The id of the record.
     */
    var id: T?
    val tableInfo: TableInfo<T, out Table<T>>

    @Suppress("UNCHECKED_CAST")
    fun update(rm: ConnectionManager) {
        rm.updateK(this, tableInfo.tbKClass as KClass<Table<T>>, tableInfo.idType)
    }

    @Suppress("UNCHECKED_CAST")
    fun delete(rm: ConnectionManager) {
        rm.deleteK(this, tableInfo.tbKClass as KClass<Table<T>>, tableInfo.idType)
    }

    @Suppress("UNCHECKED_CAST")
    fun insert(rm: ConnectionManager) {
        rm.insertK(this, tableInfo.tbKClass as KClass<Table<T>>, tableInfo.idType)
    }

    @Suppress("UNCHECKED_CAST")
    fun defined(): LazyId<Table<T>, T> {
        return LazyId.definedK(this, tableInfo.tbKClass as KClass<Table<T>>, tableInfo.idType)
    }
}

interface TableInfo<T : Any, TB : Table<T>> {
    val tbKClass: KClass<TB>
    val idType: KType

    fun find(rm: ConnectionManager, id: T): TB? {
        return rm.findK(id, tbKClass, this.idType)
    }

    @Suppress("UNCHECKED_CAST")
    fun defined(value: TB): LazyId<TB, T> = value.defined() as LazyId<TB, T>
    fun lazy(id: T, rm: ConnectionManager): LazyId<TB, T> = LazyId.lazyK(id, rm, tbKClass, idType)
}