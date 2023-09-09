package it.zoo.vendro.racoon.context

import it.zoo.vendro.racoon.connection.ConnectionManager
import kotlin.reflect.KType


/**
 * An extension of [RacoonContext] that also provides the actual class to cast to.
 *
 * @see RacoonContext
 */
class FromQueryCasterContext (
    manager: ConnectionManager,
    /**
     * The actual class to cast to.
     */
    val actualType: KType
) : RacoonContext(manager)