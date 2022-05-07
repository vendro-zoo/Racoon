package commons.casting.casters

import commons.casting.ParameterCaster

class NumberCaster : ParameterCaster<Number> {
    override fun cast(parameter: Number): String = "'$parameter'"
}