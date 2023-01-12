package it.zoo.vendro.racoon.internals.casting.builtin

import it.zoo.vendro.racoon.habitat.context.FromParameterCasterContext
import it.zoo.vendro.racoon.habitat.context.ToParameterCasterContext
import it.zoo.vendro.racoon.habitat.definition.LazyId
import it.zoo.vendro.racoon.habitat.definition.Table
import it.zoo.vendro.racoon.internals.casting.ParameterCaster
import it.zoo.vendro.racoon.internals.extensions.asKClass
import it.zoo.vendro.racoon.internals.extensions.getRuntimeGeneric
import kotlin.reflect.KClass

class LazyCaster : ParameterCaster<LazyId<Table>, Int> {
    override fun toQuery(parameter: LazyId<Table>, context: ToParameterCasterContext) =
        if (parameter.id == -1) null else parameter.id

    @Suppress("UNCHECKED_CAST")
    override fun fromQuery(parameter: Int, context: FromParameterCasterContext): LazyId<Table> {
        return LazyId.lazyK(
            parameter,
            context.manager,
            context.actualType.getRuntimeGeneric()?.asKClass() as KClass<Table>
        )
    }
}