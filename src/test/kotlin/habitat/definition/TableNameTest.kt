package habitat.definition

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TableNameTest {
    @TableName("testClass")
    class TestClass(override var id: Int? = null) : Table


    @TableName("testClass2", "tc")
    class TestClass2(override var id: Int? = null) : Table

    @Test
    fun getAliasTest() {
        assertEquals("TestClass", TableName.getAlias(TestClass::class))
    }

    @Test
    fun getAliasTest2() {
        assertEquals("tc", TableName.getAlias(TestClass2::class))
    }
}