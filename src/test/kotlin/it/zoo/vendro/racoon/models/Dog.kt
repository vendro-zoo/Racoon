package it.zoo.vendro.racoon.models

import it.zoo.vendro.racoon.definition.ColumnName
import it.zoo.vendro.racoon.definition.ColumnSetType
import it.zoo.vendro.racoon.definition.ColumnSetTypes
import it.zoo.vendro.racoon.definition.Table

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
) : Table