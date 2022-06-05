package habitat.context

import habitat.RacoonManager

/**
 * A context that is used to access in-use variables during the execution of various middleware.
 */
open class RacoonContext (
    /**
     * The in-use [RacoonManager] instance.
     */
    val manager: RacoonManager
)