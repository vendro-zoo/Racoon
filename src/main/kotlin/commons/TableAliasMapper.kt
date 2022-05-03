package commons

@Suppress("unused")
object TableAliasMappers {
    val onlyUpperToLower: (String) -> String = {
        it.filter { x -> x.isUpperCase() }.lowercase()
    }

    val onlyUpper: (String) -> String = {
        it.filter { x -> x.isUpperCase() }
    }
}