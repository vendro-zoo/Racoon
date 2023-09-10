package models

import it.zoo.vendro.racoon.definition.ColumnIgnore
import it.zoo.vendro.racoon.definition.LazyId
import it.zoo.vendro.racoon.definition.Table
import it.zoo.vendro.racoon.definition.TableInfo
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class Cat(
    override var id: Int? = null,
    var age: Int,
    var name: String?,
    var owner_id: LazyId<Int, Owner>? = null
) : Table<Int, Cat> {
    @ColumnIgnore
    override val tableInfo: TableInfo<Int, Cat> = Cats
}

object Cats : TableInfo<Int, Cat> {
    override val tbKClass: KClass<Cat> = Cat::class
    override val idType: KType = typeOf<Int>()
}