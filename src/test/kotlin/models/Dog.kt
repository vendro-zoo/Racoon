package models

import it.zoo.vendro.racoon.definition.*

enum class DogSize {
    SMALL,
    MEDIUM,
    @ColumnName("LARGE")
    L,
    ;
}

enum class DogColor {
    LIGHT,
    DARK,
    ;
}

class Dog (
    override var id: Int? = null,
    var name: String,
    @ColumnSetType(ColumnSetTypes.ObjectOther)
    @property:ColumnSetType(ColumnSetTypes.ObjectOther)
    var size: DogSize,
    @ColumnSetType(ColumnSetTypes.ObjectOther)
    @property:ColumnSetType(ColumnSetTypes.ObjectOther)
    var color: DogColor? = null
) : Table<Int> {
    @ColumnIgnore
    override val tableInfo = Dogs
}

object Dogs : TableInfo<Int, Dog> {
    override val tbKClass = Dog::class
    override val idType = kotlin.reflect.typeOf<Int>()
}