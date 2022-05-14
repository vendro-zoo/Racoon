package commons.mappers

import commons.expansions.lowerSnakeCase
import commons.expansions.upperSnakeCase

@Suppress("unused")
object NameMapper {
    val lowerSnakeCase: (String) -> String = { it.lowerSnakeCase() }

    val upperSnakeCase: (String) -> String = { it.upperSnakeCase() }

    val lowercase: (String) -> String = { it.lowercase() }

    val uppercase: (String) -> String = { it.uppercase() }
}