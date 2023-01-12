package it.zoo.vendro.racoon.habitat.context

import it.zoo.vendro.racoon.habitat.RacoonManager
import kotlin.reflect.KClass


/**
 * An extension of [RacoonContext] that also provides the actual class to cast to.
 *
 * @see RacoonContext
 */
class ToParameterCasterContext (
    manager: RacoonManager,
    /**
     * The actual class to cast to.
     */
    val actualClass: KClass<*>
) : RacoonContext(manager)