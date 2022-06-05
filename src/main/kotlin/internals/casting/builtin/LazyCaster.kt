package internals.casting.builtin

import habitat.context.ParameterCasterContext
import habitat.definition.LazyId
import habitat.definition.Table
import internals.casting.ParameterCaster
import kotlin.reflect.KClass

class LazyCaster : ParameterCaster<LazyId<Table>, Int> {
    override fun toQuery(parameter: LazyId<Table>, context: ParameterCasterContext) =
        if (parameter.id == -1) null else parameter.id

    @Suppress("UNCHECKED_CAST")
    override fun fromQuery(parameter: Int, context: ParameterCasterContext): LazyId<Table> {
        return LazyId.lazyK(
            parameter,
            context.manager,
            context.actualType as KClass<Table>
        )
    }
}