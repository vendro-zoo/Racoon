package models

import habitat.definition.LazyId
import habitat.definition.Table

class Cat(
    override var id: Int? = null,
    var age: Int,
    var name: String?,
    var owner_id: LazyId<Owner> = LazyId.empty(),
) : Table