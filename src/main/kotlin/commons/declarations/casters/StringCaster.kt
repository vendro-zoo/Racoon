package commons.declarations.casters

import commons.declarations.ParameterCaster

class StringCaster : ParameterCaster<String> {
    override fun cast(parameter: String): String = "'$parameter'"
}