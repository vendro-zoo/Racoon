package models

import habitat.definition.Table

data class Owner(
    override var id: Int? = null,
    val name: String?,
    val surname: String?,
) : Table