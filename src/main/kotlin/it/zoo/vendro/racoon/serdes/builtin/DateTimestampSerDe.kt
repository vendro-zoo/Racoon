package it.zoo.vendro.racoon.serdes.builtin

import it.zoo.vendro.racoon.context.FromQuerySerDeContext
import it.zoo.vendro.racoon.context.ToQuerySerDeContext
import it.zoo.vendro.racoon.serdes.RacoonSerDe
import java.sql.Timestamp
import java.util.*

class DateTimestampSerDe : RacoonSerDe<Date, Timestamp> {
    override fun toQuery(parameter: Date, context: ToQuerySerDeContext): Timestamp =
        Timestamp(parameter.time)

    override fun fromQuery(parameter: Timestamp, context: FromQuerySerDeContext): Date =
        Date(parameter.time)
}