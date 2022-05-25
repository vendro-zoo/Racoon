package commons.casting

import org.junit.jupiter.api.Test
import kotlin.reflect.full.primaryConstructor

internal class CastingUtilsKtTest {

    class TestClass(val value: Int)
    class TestClass2(val value: String)

    @Test
    fun castEquivalent() {
        val parameter = TestClass::class.primaryConstructor!!.parameters.first()
        val parameter2 = TestClass2::class.primaryConstructor!!.parameters.first()
        val value = 1

        castEquivalent(parameter, value)
        castEquivalent(parameter, value.toLong())
        castEquivalent(parameter, value.toShort())
        castEquivalent(parameter, value.toByte())
        castEquivalent(parameter, value.toFloat())
        castEquivalent(parameter, value.toDouble())

        castEquivalent(parameter, value.toUInt())
        castEquivalent(parameter, value.toULong())
        castEquivalent(parameter, value.toUShort())
        castEquivalent(parameter, value.toUByte())

        castEquivalent(parameter2, "test")
    }
}