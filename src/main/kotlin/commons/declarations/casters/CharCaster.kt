package commons.declarations.casters

import commons.declarations.ParameterCaster

class CharCaster : ParameterCaster<Char> {
    override fun cast(parameter: Char): String = "'$parameter'"
}