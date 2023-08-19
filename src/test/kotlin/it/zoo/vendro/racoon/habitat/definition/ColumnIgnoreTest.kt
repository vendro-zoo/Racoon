package it.zoo.vendro.racoon.habitat.definition

import it.zoo.vendro.racoon.habitat.configuration.RacoonConfiguration
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ColumnIgnoreTest {
    val config = RacoonConfiguration(
        naming = RacoonConfiguration.Naming(
            tableAliasMapper = { it },
        )
    )

    @TableName("testClass")
    class TestClass(override var id: Int? = null) : Table


    @TableName("testClass2", "tc")
    class TestClass2(override var id: Int? = null) : Table

    @Test
    fun getAliasTest() {
        assertEquals("TestClass", TableName.getAlias(TestClass::class, config))
    }

    @Test
    fun getAliasTest2() {
        assertEquals("tc", TableName.getAlias(TestClass2::class, config))
    }
}