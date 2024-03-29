package it.zoo.vendro.racoon.context

import it.zoo.vendro.racoon.connection.ConnectionManager

/**
 * A context that is used to access in-use variables during the execution of various middleware.
 */
open class RacoonContext (
    /**
     * The in-use [ConnectionManager] instance.
     */
    val manager: ConnectionManager
)