package models

import it.zoo.vendro.racoon.definition.ColumnIgnore
import it.zoo.vendro.racoon.definition.Table
import it.zoo.vendro.racoon.definition.TableInfo
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

data class Owner(
    override var id: Int? = null,
    val name: String?,
    val surname: String?,
) : Table<Int, Owner> {
    @ColumnIgnore
    override val tableInfo: TableInfo<Int, Owner> = Owners
}

object Owners : TableInfo<Int, Owner> {
    override val tbKClass: KClass<Owner> = Owner::class
    override val idType: KType = typeOf<Int>()
}
