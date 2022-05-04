package commons.configuration

data class ConnectionSettings(
    val protocol: String = "mysql",
    val host: String,
    val port: Int = 3306,
    val database: String,
    val username: String?,
    val password: String?,
) {
    override fun toString(): String {
        val stringBuilder = StringBuilder("jdbc:$protocol://$host:$port/$database?characterEncoding=UTF-8")

        if (username != null) stringBuilder.append("&user=$username")
        if (password != null) stringBuilder.append("&password=$password")

        return stringBuilder.toString()
    }
}