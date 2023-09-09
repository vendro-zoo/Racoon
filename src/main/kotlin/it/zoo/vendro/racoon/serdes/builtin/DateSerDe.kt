package it.zoo.vendro.racoon.serdes.builtin

import it.zoo.vendro.racoon.context.FromQuerySerDeContext
import it.zoo.vendro.racoon.context.ToQuerySerDeContext
import it.zoo.vendro.racoon.serdes.RacoonSerDe
import java.sql.Date
import java.sql.Date as SDate
import java.util.Date as UDate

class DateSerDe : RacoonSerDe<UDate, SDate> {
    override fun toQuery(parameter: UDate, context: ToQuerySerDeContext): Date = SDate(parameter.time)

    override fun fromQuery(parameter: SDate, context: FromQuerySerDeContext) = UDate(parameter.time)
}