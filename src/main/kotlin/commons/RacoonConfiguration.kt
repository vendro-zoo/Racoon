package commons

object RacoonConfiguration {
    var defaultConnectionSettings: ConnectionSettings? = null
    var defaultTableAliasMapper: (String) -> String = TableAliasMappers.onlyUpperToLower
}