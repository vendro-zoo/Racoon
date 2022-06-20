package internals.casting.builtin

import habitat.context.FromParameterCasterContext
import habitat.context.ToParameterCasterContext
import internals.casting.ParameterCaster
import java.sql.Date as SDate
import java.util.Date as UDate

class DateCaster : ParameterCaster<UDate, SDate> {
    override fun toQuery(parameter: UDate, context: ToParameterCasterContext) = SDate(parameter.time)

    override fun fromQuery(parameter: SDate, context: FromParameterCasterContext) = UDate(parameter.time)
}