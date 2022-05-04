package commons.declarations.casters

import commons.declarations.ParameterCaster

class BooleanCaster : ParameterCaster<Boolean> {
    override fun cast(parameter: Boolean): String = if (parameter) "true" else "false"
}