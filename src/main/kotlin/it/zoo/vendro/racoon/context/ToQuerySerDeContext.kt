package it.zoo.vendro.racoon.context

import it.zoo.vendro.racoon.connection.ConnectionManager
import kotlin.reflect.KClass


/**
 * An extension of [RacoonContext] that also provides the actual class to cast to.
 *
 * @see RacoonContext
 */
class ToQuerySerDeContext (
    manager: ConnectionManager,
    /**
     * The actual class to cast to.
     */
    val actualClass: KClass<*>
) : RacoonContext(manager)