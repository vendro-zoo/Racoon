package commons.casting.casters

import commons.casting.ParameterCaster

class BooleanCaster : ParameterCaster<Boolean> {
    override fun cast(parameter: Boolean): String = if (parameter) "true" else "false"
}