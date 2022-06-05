package habitat.context

import habitat.RacoonManager
import kotlin.reflect.KClass


// TODO: Update to use KType instead of KClass
/**
 * An extension of [RacoonContext] that also provides the actual class to cast to.
 *
 * @see RacoonContext
 */
class ParameterCasterContext (
    manager: RacoonManager,
    /**
     * The actual class to cast to.
     */
    val actualType: KClass<*>
) : RacoonContext(manager)