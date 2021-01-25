package jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite

import jp.co.soramitsu.fearless_utils.runtime.definitions.types.BaseTypeTest
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.fromHex
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.BooleanType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.u8
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.stub.Stub
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.toHex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StructTest : BaseTypeTest() {

    private val type = Struct(
        "test",
        children = linkedMapOf(
            "bool" to BooleanType,
            "u8" to u8,
        )
    )

    private val typeWithStub = Struct(
        "test",
        children = linkedMapOf(
            "stub" to Stub("bool"),
            "u8" to u8,
        )
    )

    private val expectedInstance = linkedMapOf(
        "bool" to true,
        "u8" to 9.toBigInteger()
    )

    private val expectedInHex = "0x0109"

    @Test
    fun `should return self when replace stubs`() {
        val newType = type.replaceStubs(typeRegistry)

        assert(newType === type)
    }

    @Test
    fun `should return modified copy when stubs was found`() {
        val newType = typeWithStub.replaceStubs(typeRegistry)

        assert(newType !== type)
        assertEquals(BooleanType, newType.children["stub"])
    }

    @Test
    fun `should decode instance`() {
        val decoded = type.fromHex(expectedInHex)

        assertEquals(expectedInstance, decoded.values)
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