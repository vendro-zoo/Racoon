package internals.mappers

import internals.expansions.camelCase
import internals.expansions.lowerSnakeCase
import internals.expansions.upperCamelCase
import internals.expansions.upperSnakeCase

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
     * Converts the string to UPPERCASE using the [uppercase] function.
     */
    val uppercase: (String) -> String = { it.uppercase() }

    /**
     * Converts the string to camelCase using the [camelCase] function extension.
     */
    val camelCase: (String) -> String = { it.camelCase() }

    /**
     * Converts the string to UpperCamelCase using the [upperCamelCase] function extension.
     */
    val upperCamelCase: (String) -> String = { it.upperCamelCase() }
}