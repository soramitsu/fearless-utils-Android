package jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite

import jp.co.soramitsu.fearless_utils.runtime.definitions.types.BaseTypeTest
import jp.co.soramitsu.schema.definitions.types.TypeReference
import jp.co.soramitsu.schema.definitions.types.composite.Struct
import jp.co.soramitsu.schema.definitions.types.fromHex
import jp.co.soramitsu.schema.definitions.types.primitives.BooleanType
import jp.co.soramitsu.schema.definitions.types.primitives.u8
import jp.co.soramitsu.schema.definitions.types.toHex
import org.junit.Assert.*
import org.junit.Test

class StructTest : BaseTypeTest() {

    private val type = Struct(
        "test",
        mapping = linkedMapOf(
            "bool" to TypeReference(BooleanType),
            "u8" to TypeReference(u8),
        )
    )

    private val expectedInstance = linkedMapOf(
        "bool" to true,
        "u8" to 9.toBigInteger()
    )

    private val expectedInHex = "0x0109"

    @Test
    fun `should decode instance`() {
        val decoded = type.fromHex(expectedInHex)

        assertEquals(expectedInstance, decoded.mapping)
    }

    @Test
    fun `should encode instance`() {
        val encoded = type.toHex(Struct.Instance(expectedInstance))

        assertEquals(expectedInHex, encoded)
    }

    @Test
    fun `should validate instance`() {
        val instance = Struct.Instance(expectedInstance)

        assertTrue(type.isValidInstance(instance))

        assertFalse(type.isValidInstance(1))
        assertFalse(type.isValidInstance(mapOf<String, Any>()))
        assertFalse(type.isValidInstance(Struct.Instance(mapOf<String, Any>())))
    }
}
