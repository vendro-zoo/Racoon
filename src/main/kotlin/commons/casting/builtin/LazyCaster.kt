package commons.casting.builtin

import commons.casting.ParameterCaster
import habitat.context.ParameterCasterContext
import habitat.definition.LazyId
import habitat.definition.Table
import kotlin.reflect.KClass

class LazyCaster : ParameterCaster<LazyId<Table>, Int> {
    override fun cast(parameter: LazyId<Table>, context: ParameterCasterContext) =
        if (parameter.id == -1) null else parameter.id

    @Suppress("UNCHECKED_CAST")
    override fun uncast(parameter: Int, context: ParameterCasterContext): LazyId<Table> {
        return LazyId.lazy(
            parameter,
            context.manager,
            context.actualType as KClass<Table>
        )
    }
}