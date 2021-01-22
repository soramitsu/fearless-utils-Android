package jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite

import jp.co.soramitsu.fearless_utils.runtime.definitions.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.prepopulatedTypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.fromHex
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.toHex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class EnumTest {
    private val enumValues = listOf("A", "B", "C")
    private val type = CollectionEnum("test", enumValues)

    @Test
    fun `should not replace stubs`() {
        val newType = type.replaceStubs(TypeRegistry())

        assert(newType ===  type)
    }

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