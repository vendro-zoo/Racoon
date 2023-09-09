package it.zoo.vendro.racoon.serdes.builtin

import it.zoo.vendro.racoon.context.FromQuerySerDeContext
import it.zoo.vendro.racoon.context.ToQuerySerDeContext
import it.zoo.vendro.racoon.definition.LazyId
import it.zoo.vendro.racoon.definition.Table
import it.zoo.vendro.racoon.serdes.RacoonSerDe
import it.zoo.vendro.racoon.internals.extensions.asKClass
import it.zoo.vendro.racoon.internals.extensions.getRuntimeGeneric
import java.math.BigInteger
import kotlin.reflect.KClass

class LazyBigIntegerSerDe : RacoonSerDe<LazyId<Table>?, BigInteger?> {
    override fun toQuery(parameter: LazyId<Table>?, context: ToQuerySerDeContext): BigInteger? =
        parameter?.id?.toBigInteger()

    @Suppress("UNCHECKED_CAST")
    override fun fromQuery(parameter: BigInteger?, context: FromQuerySerDeContext): LazyId<Table>? {
        val id = parameter?.toInt() ?: return null
        return LazyId.lazyK(
            id,
            context.manager,
            context.actualType.getRuntimeGeneric()?.asKClass() as KClass<Table>
        )
    }
}