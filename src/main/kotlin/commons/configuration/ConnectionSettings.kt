package commons.configuration

data class ConnectionSettings(
    /**
     * The hostname of the database.
     */
    val host: String,
    /**
     * The port of the database.
     */
    val port: Int = 3306,
    /**
     * The database name.
     */
    val database: String,
    /**
     * The username to use when connecting to the database.
     */
    val username: String? = null,
    /**
     * The password to use when connecting to the database.
     */
    val password: String? = null,
    /**
     * The timeout in milliseconds to use when connecting to the database.
     */
    val connectionTimeout: Int = 100,
    /**
     * The timeout in milliseconds to use when waiting for a response from the database
     * or when the connection is idle.
     */
    val idleTimeout: Int = 30000,
    /**
     * The maximum number of connections to keep open at any one time.
     *
     * If this value is set to 0, then there is no limit.
     *
     * The default value is 0.
     */
    val maxManagers: Int = 0
) {
    override fun toString(): String {
        val stringBuilder = StringBuilder("jdbc:${protocol}://")
            .append("$host:$port/$database")
            .append("?characterEncoding=UTF-8&")
            .append("connectionTimeout=$connectionTimeout&")
            .append("idleTimeout=$idleTimeout")

        if (username != null) stringBuilder.append("&user=$username")
        if (password != null) stringBuilder.append("&password=$password")

        return stringBuilder.toString()
    }

    companion object {
        private const val protocol: String = "mysql"
    }
}