package internals.extensions

/**
 * Checks if the character at the given index is in quotes.
 * This is done by counting the non-escaped quotes in the string.
 *
 * The character at the given index is considered in quotes
 * if the number of non-escaped quotes is odd.
 *
 * @param index The index of the character to check.
 * @return True if the character is in quotes, false otherwise.
 */
fun String.isInQuotes(index: Int): Boolean {
    // Number of quotes
    var n = 0

    var ignore = false
    for (c in this.slice(0..index)) {
        // Ignore the current character
        if (ignore) {
            ignore = false
            continue
        }

        // Checks if the current character is part of an escape sequence
        if (c == '\\') {
            ignore = true
            continue
        }

        // Checks if the current character a single quote
        if (c == '\'') n++
    }

    // The index is inside a quote if the number of quotes is odd
    return n % 2 == 1
}

/**
 * Convert a camel case string to a snake case string, leaving the first character of each word as is.
 *
 * Example:
 *
 *    "0Hello123World4".snakeCase() // "0_Hello_123_World_4"
 *
 * @return The snake case string.
 */
fun String.snakeCase(): String {
    val sb = StringBuilder()
    var wasNum = false

    this.withIndex().forEach {(i, v) ->
        if (i != 0 &&
            (
                (v.isDigit() != wasNum) ||
                    v.isUpperCase()
            )
        ) sb.append('_')
        wasNum = v.isDigit()
        sb.append(v)
    }
    return sb.toString()
}

/**
 * Convert a camel case string to a snake case string.
 *
 * Example:
 *
 *    "0Hello123World4".lowerSnakeCase() // "0_hello_123_world_4"
 *
 * @return The lower snake case string.
 */
fun String.lowerSnakeCase(): String = this.snakeCase().lowercase()


/**
 * Convert a camel case string to a screaming snake case string.
 *
 * Example:
 *
 *    "0Hello123World4".upperSnakeCase() // "0_HELLO_123_WORLD_4"
 *
 * @return The upper snake case string.
 */
fun String.upperSnakeCase(): String = this.snakeCase().uppercase()

private fun camelCase(s: String, capitalize: Boolean) =
    s.split("_")
    .withIndex()
    .joinToString("") { (i, v) ->
        if (i == 0 && !capitalize) v else v[0].uppercase() + v.substring(1).lowercase()
    }

/**
 * Convert a snake case string to a camel case string.
 *
 * Example:
 *
 *    "hello_0_world".() // "hello0World"
 *    "0_HELLO_123_WORLD_4".toUpperCamelCase() // "0Hello123World4"
 *
 * @return The camel case string.
 */
fun String.camelCase(): String = camelCase(this, false)

/**
 * Convert a snake case string to an upper camel case string.
 *
 * Example:
 *
 *    "hello_0_world".toUpperCamelCase() // "Hello0World"
 *    "0_HELLO_123_WORLD_4".toUpperCamelCase() // "0Hello123World4"
 *
 * @return The upper camel case string.
 */
fun String.upperCamelCase(): String = camelCase(this, true)