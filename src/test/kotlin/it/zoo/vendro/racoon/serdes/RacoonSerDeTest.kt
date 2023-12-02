package it.zoo.vendro.racoon.serdes

import it.zoo.vendro.racoon.TestConfiguration
import it.zoo.vendro.racoon.context.FromQuerySerDeContext
import it.zoo.vendro.racoon.context.ToQuerySerDeContext
import it.zoo.vendro.racoon.definition.*
import models.Owner
import org.junit.jupiter.api.Test
import kotlin.reflect.typeOf
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

        override fun toQuery(parameter: Int2, context: ToQuerySerDeContext): Int = parameter.i
        override fun fromQuery(parameter: Int, context: FromQuerySerDeContext): Int2 = Int2(parameter)
    }

    @TableName("cat")
    class Cat2(
        override var id: Int? = null,
        @ColumnGetType(ColumnGetTypes.Int)
        var age: Int2,
        var name: String?,
        var owner_id: LazyId<Int, Owner>? = null,
    ) : Table<Int, Cat2> {
        @ColumnIgnore
        override val tableInfo = Cats2
    }

    object Cats2 : TableInfo<Int, Cat2> {
        override val tbKClass = Cat2::class
        override val idType = typeOf<Int>()
    }

    @Test
    fun customParameterInsert() {
        pool.getManager().use { rm ->
            val cat = Cat2(age = Int2(2), name = "cat")
            cat.insert(rm)

            val ncat = Cats2.find(rm, cat.id!!)
            assertEquals(8, ncat!!.age.i)
        }
    }
}