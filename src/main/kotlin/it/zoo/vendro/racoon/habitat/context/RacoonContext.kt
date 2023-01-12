package it.zoo.vendro.racoon.habitat.context

import it.zoo.vendro.racoon.habitat.RacoonManager

/**
 * A context that is used to access in-use variables during the execution of various middleware.
 */
open class RacoonContext (
    /**
     * The in-use [RacoonManager] instance.
     */
    val manager: RacoonManager
)