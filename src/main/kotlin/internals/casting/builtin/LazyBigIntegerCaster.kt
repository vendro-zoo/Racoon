package internals.casting.builtin

import habitat.context.FromParameterCasterContext
import habitat.context.ToParameterCasterContext
import habitat.definition.LazyId
import habitat.definition.Table
import internals.casting.ParameterCaster
import internals.extensions.asKClass
import internals.extensions.getRuntimeGeneric
import java.math.BigInteger
import kotlin.reflect.KClass

class LazyBigIntegerCaster : ParameterCaster<LazyId<Table>, BigInteger> {
    override fun toQuery(parameter: LazyId<Table>, context: ToParameterCasterContext) =
        if (parameter.id == -1) null else parameter.id?.toBigInteger()

    @Suppress("UNCHECKED_CAST")
    override fun fromQuery(parameter: BigInteger, context: FromParameterCasterContext): LazyId<Table> {
        return LazyId.lazyK(
            parameter.toInt(),
            context.manager,
            context.actualType.getRuntimeGeneric()?.asKClass() as KClass<Table>
        )
    }
}