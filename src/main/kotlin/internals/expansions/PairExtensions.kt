package internals.expansions

fun <T, K> Pair<T, K>.asMapEntry() = listOf(this).toMap().entries.first()