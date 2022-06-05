package internals.casting.builtin

import habitat.context.FromParameterCasterContext
import habitat.context.ToParameterCasterContext
import habitat.definition.LazyId
import habitat.definition.Table
import internals.casting.ParameterCaster
import internals.expansions.asKClass
import internals.expansions.getRuntimeGeneric
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