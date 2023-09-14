package it.zoo.vendro.racoon.serdes.builtin

import it.zoo.vendro.racoon.context.FromQuerySerDeContext
import it.zoo.vendro.racoon.context.ToQuerySerDeContext
import it.zoo.vendro.racoon.definition.LazyId
import it.zoo.vendro.racoon.definition.Table
import it.zoo.vendro.racoon.internals.extensions.asKClass
import it.zoo.vendro.racoon.internals.extensions.getRuntimeGeneric
import it.zoo.vendro.racoon.serdes.RacoonSerDe
import it.zoo.vendro.racoon.serdes.castEquivalentK
import kotlin.reflect.KClass

class LazyIdSerDe<TB : Table<Any, TB>> : RacoonSerDe<LazyId<*, TB>?, Any?> {
    override fun toQuery(parameter: LazyId<*, TB>?, context: ToQuerySerDeContext): Any? =
        parameter?.id

    @Suppress("UNCHECKED_CAST")
    override fun fromQuery(parameter: Any?, context: FromQuerySerDeContext): LazyId<Any, TB>? {
        val id = parameter?.let {
            castEquivalentK(
                context.actualType.getRuntimeGeneric()!!.asKClass(),
                parameter
            )
        } ?: return null

        val tableClass = context.actualType.getRuntimeGeneric(1)!!.asKClass() as KClass<TB>
        return LazyId.lazyK(
            id,
            context.manager,
            tableClass,
            context.actualType.getRuntimeGeneric()!!
        )
    }
}