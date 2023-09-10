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
    fun update(cm: ConnectionManager) {
        cm.updateK(this, tableInfo.tbKClass as KClass<Table<T, TB>>, tableInfo.idType)
    }

    @Suppress("UNCHECKED_CAST")
    fun delete(cm: ConnectionManager) {
        cm.deleteK(this, tableInfo.tbKClass as KClass<Table<T, TB>>, tableInfo.idType)
    }

    @Suppress("UNCHECKED_CAST")
    fun insert(cm: ConnectionManager) {
        cm.insertK(this, tableInfo.tbKClass as KClass<Table<T, TB>>, tableInfo.idType)
    }

    @Suppress("UNCHECKED_CAST")
    fun defined(): LazyId<T, TB> {
        return LazyId.definedK(this as TB, tableInfo.tbKClass, tableInfo.idType)
    }
}

interface TableInfo<T : Any, TB : Table<T, TB>> {
    val tbKClass: KClass<TB>
    val idType: KType

    fun find(cm: ConnectionManager, id: T): TB? {
        return cm.findK(id, tbKClass, this.idType)
    }

    fun defined(value: TB): LazyId<T, TB> = value.defined()
    fun lazy(id: T, cm: ConnectionManager): LazyId<T, TB> = LazyId.lazyK(id, cm, tbKClass, idType)
}