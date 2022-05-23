package models

import habitat.definition.LazyId
import habitat.definition.Table

data class Cat(
    override var id: Int? = null,
    var name: String? = null,
    var owner_id: LazyId<Owner>? = null
) : Table