package it.zoo.vendro.racoon.serdes.builtin

import it.zoo.vendro.racoon.context.FromQueryCasterContext
import it.zoo.vendro.racoon.context.ToQueryCasterContext
import it.zoo.vendro.racoon.definition.LazyId
import it.zoo.vendro.racoon.definition.Table
import it.zoo.vendro.racoon.serdes.RacoonSerDe
import it.zoo.vendro.racoon.internals.extensions.asKClass
import it.zoo.vendro.racoon.internals.extensions.getRuntimeGeneric
import kotlin.reflect.KClass

class LazyLongSerDe : RacoonSerDe<LazyId<Table>?, Long?> {
    override fun toQuery(parameter: LazyId<Table>?, context: ToQueryCasterContext): Long? =
        parameter?.id?.toLong()

    @Suppress("UNCHECKED_CAST")
    override fun fromQuery(parameter: Long?, context: FromQueryCasterContext): LazyId<Table>? {
        val id = parameter?.toInt() ?: return null
        return LazyId.lazyK(
            id,
            context.manager,
            context.actualType.getRuntimeGeneric()?.asKClass() as KClass<Table>
        )
    }
}