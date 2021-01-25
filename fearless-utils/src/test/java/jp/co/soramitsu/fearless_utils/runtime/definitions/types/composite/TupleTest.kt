package jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite

import jp.co.soramitsu.fearless_utils.runtime.definitions.types.BaseTypeTest
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.fromHex
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.BooleanType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.u8
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.stub.Stub
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.toHex
import org.junit.Assert.*
import org.junit.Test

class TupleTest : BaseTypeTest() {
    private val type = Tuple(
        "test",
        listOf(
            BooleanType,
            u8,
        )
    )

    private val typeWithStub = Tuple(
        "test",
        listOf(
            Stub("bool"),
            u8,
        )
    )

    private val expectedInstance = listOf(
        true,
        9.toBigInteger()
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
        assertEquals(BooleanType, newType.types.first())
    }

    @Test
    fun `should decode instance`() {
        val decoded = type.fromHex(expectedInHex)

        assertEquals(expectedInstance, decoded)
    }

    @Test
    fun `should encode instance`() {
        val encoded = type.toHex(expectedInstance)

        assertEquals(expectedInHex, encoded)
    }

    @Test
    fun `should validate instance`() {
        assertTrue(type.isValidInstance(expectedInstance))

        assertFalse(type.isValidInstance(1))
        assertFalse(type.isValidInstance(listOf<Any?>(false)))
    }
}