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