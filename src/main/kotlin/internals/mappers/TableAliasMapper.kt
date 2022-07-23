package internals.mappers

/**
 * An object containing a set of lambdas used to map class's name to a table alias.
 */
@Suppress("unused")
object TableAliasMapper {
    /**
     * Extracts only the uppercase letters from the given string, and converts the resulting string to lowercase.
     *
     * @return the extracted uppercase letters from the given string, converted to lowercase.
     */
    val onlyUpperToLower: (String) -> String = { onlyUpper(it).lowercase() }

    /**
     * Extracts only the uppercase letters from the given string.
     *
     * @return The extracted uppercase letters.
     */
    val onlyUpper: (String) -> String = { it.filter { x -> x.isUpperCase() } }
}