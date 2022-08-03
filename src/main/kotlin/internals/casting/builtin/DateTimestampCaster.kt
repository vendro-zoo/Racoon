package internals.casting.builtin

import habitat.context.FromParameterCasterContext
import habitat.context.ToParameterCasterContext
import internals.casting.ParameterCaster
import java.sql.Timestamp
import java.util.*

class DateTimestampCaster : ParameterCaster<Date, Timestamp> {
    override fun toQuery(parameter: Date, context: ToParameterCasterContext): Timestamp =
        Timestamp(parameter.time)

    override fun fromQuery(parameter: Timestamp, context: FromParameterCasterContext): Date =
        Date(parameter.time)
}