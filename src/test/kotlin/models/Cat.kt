package models

import it.zoo.vendro.racoon.definition.LazyId
import it.zoo.vendro.racoon.definition.Table

class Cat(
    override var id: Int? = null,
    var age: Int,
    var name: String?,
    var owner_id: LazyId<Owner>? = null
) : Table