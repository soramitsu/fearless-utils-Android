package jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite

import jp.co.soramitsu.fearless_utils.common.assertInstance
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.BaseTypeTest
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.fromHex
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.BooleanType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.UIntType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.stub.Stub
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.toHex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VecTest : BaseTypeTest() {
    private val typeInstance = listOf(
        true, false, true, true
    )

    private val type = Vec(
        "test",
        BooleanType
    )

    private val inHex = "0x1001000101"

    @Test
    fun `should return self if no stubs found`() {
        val newType = type.replaceStubs(typeRegistry)

        assert(newType === type)
    }

    @Test
    fun `should return modified copy if stubs were found`() {
        val typeWithStubs = Vec(
            "test",
            Stub("u8")
        )

        val newType = typeWithStubs.replaceStubs(typeRegistry)

        assert(newType !== typeWithStubs)
        assertInstance<UIntType>(newType.type)
    }

    @Test
    fun `should decode instance`() {
        val decoded = type.fromHex(inHex)

        assertEquals(typeInstance, decoded)
    }

    @Test
    fun `should encode instance`() {
        val encoded = type.toHex(typeInstance)

        assertEquals(inHex, encoded)
    }

    @Test
    fun `should validate instance`() {
        assertTrue(type.isValidInstance(typeInstance))
        assertTrue(type.isValidInstance(listOf(false)))
        assertTrue(type.isValidInstance(listOf(false, true)))

        assertFalse(type.isValidInstance(listOf(1)))
        assertFalse(type.isValidInstance(1))
    }
}