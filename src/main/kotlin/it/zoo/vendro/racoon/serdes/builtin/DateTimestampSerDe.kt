package it.zoo.vendro.racoon.serdes.builtin

import it.zoo.vendro.racoon.context.FromQueryCasterContext
import it.zoo.vendro.racoon.context.ToQueryCasterContext
import it.zoo.vendro.racoon.serdes.RacoonSerDe
import java.sql.Timestamp
import java.util.*

class DateTimestampSerDe : RacoonSerDe<Date, Timestamp> {
    override fun toQuery(parameter: Date, context: ToQueryCasterContext): Timestamp =
        Timestamp(parameter.time)

    override fun fromQuery(parameter: Timestamp, context: FromQueryCasterContext): Date =
        Date(parameter.time)
}