package commons.declarations.casters

import commons.declarations.ParameterCaster
import java.text.SimpleDateFormat
import java.util.*

class DateCaster : ParameterCaster<Date> {
    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    override fun cast(parameter: Date): String = formatter.format(parameter)
}