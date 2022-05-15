package commons.expansions

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
 * Convert an upper camel case string to a snake case string, leaving the first character of each word as is.
 *
 * Example:
 *
 *    "0Hello123World4oSnakeCase() // "0_Hello_123_World_4"
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
 * Convert an upper camel case string to a lower snake case string.
 *
 * Example:
 *
 *    "0Hello123World4".toLowerSnakeCase() // "0_hello_123_world_4"
 *
 * @return The lower snake case string.
 */
fun String.lowerSnakeCase(): String = this.snakeCase().lowercase()


/**
 * Convert an upper camel case string to an upper snake case string.
 *
 * Example:
 *
 *    "0Hello123World4".toUpperSnakeCase() // "0_HELLO_123_WORLD_4"
 *
 * @return The upper snake case string.
 */
fun String.upperSnakeCase(): String = this.snakeCase().uppercase()