package it.zoo.vendro.racoon.serdes.builtin

import it.zoo.vendro.racoon.context.FromQuerySerDeContext
import it.zoo.vendro.racoon.context.ToQuerySerDeContext
import it.zoo.vendro.racoon.definition.LazyId
import it.zoo.vendro.racoon.definition.Table
import it.zoo.vendro.racoon.internals.extensions.asKClass
import it.zoo.vendro.racoon.internals.extensions.getRuntimeGeneric
import it.zoo.vendro.racoon.serdes.RacoonSerDe
import kotlin.reflect.KClass

class LazySerDe : RacoonSerDe<LazyId<Table<*>, *>?, Any?> {
    override fun toQuery(parameter: LazyId<Table<*>, *>?, context: ToQuerySerDeContext): Any? =
        parameter?.id

    @Suppress("UNCHECKED_CAST")
    override fun fromQuery(parameter: Any?, context: FromQuerySerDeContext): LazyId<Table<*>, *>? {
        val id = parameter ?: return null
        val tableClass = context.actualType.getRuntimeGeneric()?.asKClass() as KClass<Table<Any>>
        return LazyId.lazyK(
            id,
            context.manager,
            tableClass,
            context.actualType.getRuntimeGeneric()!!
        ) as LazyId<Table<*>, *>
    }
}