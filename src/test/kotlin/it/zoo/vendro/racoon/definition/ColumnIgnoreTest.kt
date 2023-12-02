package it.zoo.vendro.racoon.definition

import it.zoo.vendro.racoon.configuration.RacoonConfiguration
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlin.test.assertEquals

class ColumnIgnoreTest {
    val config = RacoonConfiguration(
        naming = RacoonConfiguration.Naming(
            tableAliasMapper = { it },
        )
    )

    @TableName("testClass")
    class TestClass(override var id: Int? = null) : Table<Int, TestClass> {
        override val tableInfo = TestClasses
    }

    object TestClasses : TableInfo<Int, TestClass> {
        override val tbKClass: KClass<TestClass> = TestClass::class
        override val idType: KType = typeOf<Int>()

    }


    @TableName("testClass2", "tc")
    class TestClass2(override var id: Int? = null) : Table<Int, TestClass2> {
        override val tableInfo = TestClasses2
    }

    object TestClasses2 : TableInfo<Int, TestClass2> {
        override val tbKClass: KClass<TestClass2> = TestClass2::class
        override val idType: KType = typeOf<Int>()
    }

    @Test
    fun getAliasTest() {
        assertEquals("TestClass", TableName.getAlias(TestClass::class, config))
    }

    @Test
    fun getAliasTest2() {
        assertEquals("tc", TableName.getAlias(TestClass2::class, config))
    }
}