package it.zoo.vendro.racoon.internals.casting.builtin

import it.zoo.vendro.racoon.habitat.context.FromParameterCasterContext
import it.zoo.vendro.racoon.habitat.context.ToParameterCasterContext
import it.zoo.vendro.racoon.internals.casting.ParameterCaster
import java.sql.Timestamp
import java.util.*

class DateTimestampCaster : ParameterCaster<Date, Timestamp> {
    override fun toQuery(parameter: Date, context: ToParameterCasterContext): Timestamp =
        Timestamp(parameter.time)

    override fun fromQuery(parameter: Timestamp, context: FromParameterCasterContext): Date =
        Date(parameter.time)
}