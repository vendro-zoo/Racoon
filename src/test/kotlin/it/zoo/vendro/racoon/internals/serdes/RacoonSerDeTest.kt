package it.zoo.vendro.racoon.internals.serdes

import it.zoo.vendro.racoon.TestConfiguration
import it.zoo.vendro.racoon.context.FromQueryCasterContext
import it.zoo.vendro.racoon.context.ToQueryCasterContext
import it.zoo.vendro.racoon.definition.*
import it.zoo.vendro.racoon.models.Owner
import it.zoo.vendro.racoon.serdes.RacoonSerDe
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class RacoonSerDeTest {
    val pool = TestConfiguration.POOL.apply {
        this.configuration.casting.setCaster(Int2SerDe())
    }

    data class Int2(val i: Int)

    class Int2SerDe : RacoonSerDe<Int2, Int> {
        override val toQueryPostfix: String
            get() = "*2"
        override val fromQueryPostfix: String
            get() = "*2"

        override fun toQuery(parameter: Int2, context: ToQueryCasterContext): Int = parameter.i
        override fun fromQuery(parameter: Int, context: FromQueryCasterContext): Int2 = Int2(parameter)
    }

    @TableName("cat")
    class Cat2(
        override var id: Int? = null,
        @ColumnGetType(ColumnGetTypes.Int)
        var age: Int2,
        var name: String?,
        var owner_id: LazyId<Owner>? = null,
    ) : Table

    @Test
    fun customParameterInsert() {
        pool.getManager().use { rm ->
            val cat = Cat2(age = Int2(2), name = "cat")
            rm.insert(cat)

            val ncat = rm.find<Cat2>(cat.id!!)
            assertEquals(8, ncat!!.age.i)
        }
    }
}