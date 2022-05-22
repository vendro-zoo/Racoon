package commons.mappers

import commons.expansions.lowerSnakeCase
import commons.expansions.upperSnakeCase

@Suppress("unused")
object NameMapper {
    /**
     * Converts a camelCase string to snake_case using the [lowerSnakeCase] function extension.
     */
    val lowerSnakeCase: (String) -> String = { it.lowerSnakeCase() }

    /**
     * Converts a camelCase string to UPPER_SNAKE_CASE using the [upperSnakeCase] function extension.
     */
    val upperSnakeCase: (String) -> String = { it.upperSnakeCase() }

    /**
     * Converts the string to lowercase using the [lowercase] function.
     */
    val lowercase: (String) -> String = { it.lowercase() }

    /**
     * Converts the string to uppercase using the [uppercase] function.
     */
    val uppercase: (String) -> String = { it.uppercase() }
}