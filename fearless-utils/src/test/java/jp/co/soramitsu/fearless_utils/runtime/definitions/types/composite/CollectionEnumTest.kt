package jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite

import jp.co.soramitsu.fearless_utils.runtime.definitions.types.BaseTypeTest
import jp.co.soramitsu.schema.definitions.types.composite.CollectionEnum
import jp.co.soramitsu.schema.definitions.types.fromHex
import jp.co.soramitsu.schema.definitions.types.toHex
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class EnumTest : BaseTypeTest() {
    private val enumValues = listOf("A", "B", "C")
    private val type = CollectionEnum("test", enumValues)

    @Test
    fun `should decode instance`() {
        val expectedInstance = enumValues[1]
        val inHex = "0x01"

        val decoded = type.fromHex(inHex)

        assertEquals(expectedInstance, decoded)
    }

    @Test
    fun `should encode instance`() {
        val instance = enumValues[1]

        val encoded = type.toHex(instance)

        assertEquals("0x01", encoded)
    }

    @Test
    fun `should validate instance`() {
        assertTrue(type.isValidInstance("A"))

        assertFalse(type.isValidInstance("F"))

        assertFalse(type.isValidInstance(1))
    }
}
