package it.zoo.vendro.internals.casting

import it.zoo.vendro.habitat.definition.*
import it.zoo.vendro.racoon.habitat.ConnectionPool
import it.zoo.vendro.racoon.habitat.configuration.RacoonConfiguration
import it.zoo.vendro.racoon.habitat.context.FromParameterCasterContext
import it.zoo.vendro.racoon.habitat.context.ToParameterCasterContext
import it.zoo.vendro.racoon.habitat.definition.*
import it.zoo.vendro.racoon.internals.casting.ParameterCaster
import it.zoo.vendro.racoon.internals.configuration.ConnectionSettings
import it.zoo.vendro.racoon.models.Owner
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class ParameterCasterTest {
    data class Int2(val i: Int)

    class Int2Caster : ParameterCaster<Int2, Int> {
        override val toQueryPostfix: String
            get() = "*2"
        override val fromQueryPostfix: String
            get() = "*2"

        override fun toQuery(parameter: Int2, context: ToParameterCasterContext): Int = parameter.i
        override fun fromQuery(parameter: Int, context: FromParameterCasterContext): Int2 = Int2(parameter)
    }

    @TableName("cat")
    class Cat2(
        override var id: Int? = null,
        @ColumnExtractionMethod(ExtractionMethodType.Int)
        var age: Int2,
        var name: String?,
        var owner_id: LazyId<Owner>? = null,
    ) : Table

    companion object {
        @BeforeAll
        @JvmStatic
        fun setUp() {
            RacoonConfiguration.Connection.connectionSettings =
                ConnectionSettings(
                    host = "localhost",
                    port = 3306,
                    database = "racoon-ktor-sample",
                    username = "admin",
                    password = "admin",
                    idleTimeout = 300
                )
            RacoonConfiguration.Casting.setCaster(Int2::class, Int::class, Int2Caster())
        }
    }

    @Test
    fun customParameterInsert() {
        ConnectionPool.getManager().use { rm ->
            val cat = Cat2(age = Int2(2), name = "cat")
            rm.insert(cat)

            val ncat = rm.find<Cat2>(cat.id!!)
            assertEquals(8, ncat!!.age.i)
        }
    }
}