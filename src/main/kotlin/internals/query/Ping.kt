package internals.query

/**
 * A data class only containing a property named `ping`.
 *
 * Used to check if the connection to the database is still alive.
 */
data class Ping(val ping: Byte)
