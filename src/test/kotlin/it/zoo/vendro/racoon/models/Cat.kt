package it.zoo.vendro.racoon.models

import it.zoo.vendro.racoon.habitat.definition.LazyId
import it.zoo.vendro.racoon.habitat.definition.Table

class Cat(
    override var id: Int? = null,
    var age: Int,
    var name: String?,
    var owner_id: LazyId<Owner>? = null
) : Table