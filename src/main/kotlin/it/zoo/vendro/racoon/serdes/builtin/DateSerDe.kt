package it.zoo.vendro.racoon.serdes.builtin

import it.zoo.vendro.racoon.context.FromQueryCasterContext
import it.zoo.vendro.racoon.context.ToQueryCasterContext
import it.zoo.vendro.racoon.serdes.RacoonSerDe
import java.sql.Date
import java.sql.Date as SDate
import java.util.Date as UDate

class DateSerDe : RacoonSerDe<UDate, SDate> {
    override fun toQuery(parameter: UDate, context: ToQueryCasterContext): Date = SDate(parameter.time)

    override fun fromQuery(parameter: SDate, context: FromQueryCasterContext) = UDate(parameter.time)
}