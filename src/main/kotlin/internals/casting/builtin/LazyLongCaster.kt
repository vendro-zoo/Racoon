package internals.casting.builtin

import habitat.context.FromParameterCasterContext
import habitat.context.ToParameterCasterContext
import habitat.definition.LazyId
import habitat.definition.Table
import internals.casting.ParameterCaster
import internals.extensions.asKClass
import internals.extensions.getRuntimeGeneric
import kotlin.reflect.KClass

class LazyLongCaster : ParameterCaster<LazyId<Table>, Long> {
    override fun toQuery(parameter: LazyId<Table>, context: ToParameterCasterContext) =
        if (parameter.id == -1) null else parameter.id?.toLong()

    @Suppress("UNCHECKED_CAST")
    override fun fromQuery(parameter: Long, context: FromParameterCasterContext): LazyId<Table> {
        return LazyId.lazyK(
            parameter.toInt(),
            context.manager,
            context.actualType.getRuntimeGeneric()?.asKClass() as KClass<Table>
        )
    }
}