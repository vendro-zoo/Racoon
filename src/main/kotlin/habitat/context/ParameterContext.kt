package habitat.context

import habitat.RacoonManager
import kotlin.reflect.KClass

class ParameterCasterContext (
    manager: RacoonManager,
    val actualType: KClass<*>
) : RacoonContext(manager)