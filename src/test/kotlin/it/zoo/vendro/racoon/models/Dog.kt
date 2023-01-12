package it.zoo.vendro.racoon.models

import it.zoo.vendro.racoon.habitat.definition.ColumnName
import it.zoo.vendro.racoon.habitat.definition.Table

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
    var size: DogSize,
    var color: DogColor? = null
) : Table