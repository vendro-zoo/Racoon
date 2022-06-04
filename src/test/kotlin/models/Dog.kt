package models

import habitat.definition.ColumnName
import habitat.definition.Table

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