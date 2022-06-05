package internals.extensions

/**
 * Converts a the [Pair] to a [Map.Entry],
 * where the key is the first element of the pair and the value is the second element of the pair.
 *
 * @return the [Map.Entry]
 */
fun <T, K> Pair<T, K>.asMapEntry() = listOf(this).toMap().entries.first()