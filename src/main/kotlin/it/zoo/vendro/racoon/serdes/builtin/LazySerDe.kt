package it.zoo.vendro.racoon.serdes.builtin

import it.zoo.vendro.racoon.context.FromQueryCasterContext
import it.zoo.vendro.racoon.context.ToQueryCasterContext
import it.zoo.vendro.racoon.definition.LazyId
import it.zoo.vendro.racoon.definition.Table
import it.zoo.vendro.racoon.serdes.RacoonSerDe
import it.zoo.vendro.racoon.internals.extensions.asKClass
import it.zoo.vendro.racoon.internals.extensions.getRuntimeGeneric
import kotlin.reflect.KClass

class LazySerDe : RacoonSerDe<LazyId<Table>?, Int?> {
    override fun toQuery(parameter: LazyId<Table>?, context: ToQueryCasterContext): Int? =
        parameter?.id

    @Suppress("UNCHECKED_CAST")
    override fun fromQuery(parameter: Int?, context: FromQueryCasterContext): LazyId<Table>? {
        val id = parameter ?: return null
        return LazyId.lazyK(
            id,
            context.manager,
            context.actualType.getRuntimeGeneric()?.asKClass() as KClass<Table>
        )
    }
}