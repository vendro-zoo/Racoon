package it.zoo.vendro.racoon.internals.casting.builtin

import it.zoo.vendro.racoon.habitat.context.FromParameterCasterContext
import it.zoo.vendro.racoon.habitat.context.ToParameterCasterContext
import it.zoo.vendro.racoon.internals.casting.ParameterCaster
import java.sql.Date as SDate
import java.util.Date as UDate

class DateCaster : ParameterCaster<UDate, SDate> {
    override fun toQuery(parameter: UDate, context: ToParameterCasterContext) = SDate(parameter.time)

    override fun fromQuery(parameter: SDate, context: FromParameterCasterContext) = UDate(parameter.time)
}