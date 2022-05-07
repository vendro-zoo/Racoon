package commons.casting.casters

import commons.casting.ParameterCaster

class CharCaster : ParameterCaster<Char> {
    override fun cast(parameter: Char): String = "'$parameter'"
}