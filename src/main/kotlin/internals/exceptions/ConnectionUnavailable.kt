package internals.exceptions

class ConnectionUnavailable(message: String = "The connection is unavailable", cause: Throwable? = null) : Exception(message, cause) {
    constructor(cause: Throwable) : this(cause.message ?: "The connection is unavailable", cause)
}