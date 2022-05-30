package commons.mappers

@Suppress("unused")
object TableAliasMapper {
    /**
     * Extracts only the uppercase letters from the given string, and converts the resulting string to lowercase.
     *
     * @param string the string to extract the uppercase letters from.
     * @return the extracted uppercase letters from the given string, converted to lowercase.
     */
    val onlyUpperToLower: (String) -> String = { onlyUpper(it).lowercase() }

    /**
     * Extracts only the uppercase letters from the given string.
     *
     * @param string The string to extract the uppercase letters from.
     * @return The extracted uppercase letters.
     */
    val onlyUpper: (String) -> String = { it.filter { x -> x.isUpperCase() } }
}