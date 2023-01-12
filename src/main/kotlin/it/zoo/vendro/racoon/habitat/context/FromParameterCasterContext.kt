package it.zoo.vendro.racoon.habitat.context

import it.zoo.vendro.racoon.habitat.ConnectionManager
import kotlin.reflect.KType


/**
 * An extension of [RacoonContext] that also provides the actual class to cast to.
 *
 * @see RacoonContext
 */
class FromParameterCasterContext (
    manager: ConnectionManager,
    /**
     * The actual class to cast to.
     */
    val actualType: KType
) : RacoonContext(manager)