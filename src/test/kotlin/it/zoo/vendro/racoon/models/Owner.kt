package it.zoo.vendro.racoon.models

import it.zoo.vendro.racoon.habitat.definition.Table

data class Owner(
    override var id: Int? = null,
    val name: String?,
    val surname: String?,
) : Table