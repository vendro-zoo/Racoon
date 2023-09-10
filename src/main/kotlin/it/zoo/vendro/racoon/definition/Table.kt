package it.zoo.vendro.racoon.definition

import it.zoo.vendro.racoon.connection.ConnectionManager
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * Indicates a class that defines a table in the database.
 */
interface Table<T : Any, TB : Table<T, TB>> {
    /**
     * The id of the record.
     */
    var id: T?
    val tableInfo: TableInfo<T, TB>

    @Suppress("UNCHECKED_CAST")
    fun update(rm: ConnectionManager) {
        rm.updateK(this, tableInfo.tbKClass as KClass<Table<T, TB>>, tableInfo.idType)
    }

    @Suppress("UNCHECKED_CAST")
    fun delete(rm: ConnectionManager) {
        rm.deleteK(this, tableInfo.tbKClass as KClass<Table<T, TB>>, tableInfo.idType)
    }

    @Suppress("UNCHECKED_CAST")
    fun insert(rm: ConnectionManager) {
        rm.insertK(this, tableInfo.tbKClass as KClass<Table<T, TB>>, tableInfo.idType)
    }

    @Suppress("UNCHECKED_CAST")
    fun defined(): LazyId<T, TB> {
        return LazyId.definedK(this as TB, tableInfo.tbKClass, tableInfo.idType)
    }
}

interface TableInfo<T : Any, TB : Table<T, TB>> {
    val tbKClass: KClass<TB>
    val idType: KType

    fun find(rm: ConnectionManager, id: T): TB? {
        return rm.findK(id, tbKClass, this.idType)
    }

    fun defined(value: TB): LazyId<T, TB> = value.defined()
    fun lazy(id: T, rm: ConnectionManager): LazyId<T, TB> = LazyId.lazyK(id, rm, tbKClass, idType)
}