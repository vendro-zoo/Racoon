package commons.casting.casters

import commons.casting.ParameterCaster

class StringCaster : ParameterCaster<String> {
    override fun cast(parameter: String): String = "'$parameter'"
}