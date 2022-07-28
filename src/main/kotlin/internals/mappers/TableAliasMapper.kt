package internals.mappers

/**
 * An object containing a set of lambdas used to map class's name to a table alias.
 */
@Suppress("unused")
object TableAliasMapper {
    /**
     * Extracts only the non-sequential uppercase letters from the given string, and converts the resulting string to lowercase.
     *
     * @return the extracted non-sequential uppercase letters from the given string, converted to lowercase.
     */
    val onlyUpperNonSequentialToLower: (String) -> String = { onlyUpperNonSequential(it).lowercase() }

    /**
     * Extracts only the non-sequential uppercase letters from the given string.
     *
     * @return The extracted non-sequential uppercase letters.
     */
    val onlyUpperNonSequential: (String) -> String = { s ->
        s.withIndex().filter { (index, char) ->
            char.isUpperCase() && (
                    index == 0 ||
                            index > 0 && s[index - 1].isLowerCase() ||
                            index < s.length - 1 && s[index + 1].isLowerCase()

                    )
        }.map { it.value }.joinToString("")
    }

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