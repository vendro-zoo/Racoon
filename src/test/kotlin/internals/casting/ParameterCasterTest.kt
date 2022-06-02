package internals.casting

import habitat.RacoonDen
import habitat.configuration.RacoonConfiguration
import habitat.context.ParameterCasterContext
import habitat.definition.LazyId
import habitat.definition.Table
import habitat.definition.TableName
import internals.configuration.ConnectionSettings
import models.Owner
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

        override fun toQuery(parameter: Int2, context: ParameterCasterContext): Int = parameter.i
        override fun fromQuery(parameter: Int, context: ParameterCasterContext): Int2 = Int2(parameter)
    }

    @TableName("cat")
    class Cat2(
        override var id: Int? = null,
        var age: Int2,
        var name: String?,
        var owner_id: LazyId<Owner>,
    ) : Table

    companion object {
        @BeforeAll
        @JvmStatic
        fun setUp() {
            RacoonConfiguration.Connection.setDefault(
                ConnectionSettings(
                    host = "localhost",
                    port = 3306,
                    database = "racoon-ktor-sample",
                    username = "admin",
                    password = "admin",
                    idleTimeout = 3
                )
            )
            RacoonConfiguration.Casting.setCaster(Int2::class, Int2Caster())
        }
    }

    @Test
    fun customParameterInsert() {
        RacoonDen.getManager().use { rm ->
            val cat = Cat2(age = Int2(2), name = "cat", owner_id = LazyId.empty())
            rm.insert(cat)

            val ncat = rm.find<Cat2>(cat.id!!)
            assertEquals(8, ncat!!.age.i)
        }
    }
}