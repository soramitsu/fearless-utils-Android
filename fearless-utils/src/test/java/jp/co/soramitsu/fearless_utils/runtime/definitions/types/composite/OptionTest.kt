package jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite

import jp.co.soramitsu.fearless_utils.common.assertInstance
import jp.co.soramitsu.fearless_utils.common.assertThrows
import jp.co.soramitsu.fearless_utils.runtime.definitions.prepopulatedTypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.fromHex
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.BooleanType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.UIntType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.u8
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.stub.Stub
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.toHex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test


class OptionTest {

    private val typeRegistry = prepopulatedTypeRegistry()

    private val optionalBoolean = Option(
        "test",
        BooleanType
    )

    private val optionalU8 = Option(
        "test",
        u8
    )

    @Test
    fun `should return self if no stubs found`() {
        val newType = optionalBoolean.replaceStubs(typeRegistry)

        assert(newType === optionalBoolean)
    }

    @Test
    fun `should return modified copy if stubs were found`() {
        val typeWithStubs = Option(
            "test",
            Stub("u8")
        )

        val newType = typeWithStubs.replaceStubs(typeRegistry)

        assert(newType !== typeWithStubs)
        assertInstance<UIntType>(newType.type)
    }

    @Test
    fun `should decode optional true`() {
        val inHex = "0x02"
        val decoded = optionalBoolean.fromHex(inHex)

        assertEquals(true, decoded)
    }

    @Test
    fun `should throw for wrong optional boolean value`() {
        val inHex = "0x05"

        assertThrows<IllegalArgumentException> {
            optionalBoolean.fromHex(inHex)
        }
    }

    @Test
    fun `should decode optional false`() {
        val inHex = "0x01"
        val decoded = optionalBoolean.fromHex(inHex)

        assertEquals(false, decoded)
    }

    @Test
    fun `should decode null boolean`() {
        val inHex = "0x00"
        val decoded = optionalBoolean.fromHex(inHex)

        assertEquals(null, decoded)
    }

    @Test
    fun `should decode non-null other type`() {
        val inHex = "0x0109"
        val decoded = optionalU8.fromHex(inHex)

        assertEquals(9.toBigInteger(), decoded)
    }

    @Test
    fun `should decode null other type`() {
        val inHex = "0x00"
        val decoded = optionalU8.fromHex(inHex)

        assertEquals(null, decoded)
    }

    @Test
    fun `should encode non-boolean instance`() {
        val encoded = optionalU8.toHex(9.toBigInteger())

        assertEquals("0x0109", encoded)
    }

    @Test
    fun `should encode null non-boolean instance`() {
        val encoded = optionalU8.toHex(null)

        assertEquals("0x00", encoded)
    }

    @Test
    fun `should encode boolean instance`() {
        val encoded = optionalBoolean.toHex(true)

        assertEquals("0x02", encoded)
    }

    @Test
    fun `should validate instance`() {
        assertTrue(optionalBoolean.isValidInstance(null))
        assertTrue(optionalBoolean.isValidInstance(true))
        assertTrue(optionalU8.isValidInstance(4.toBigInteger()))

        assertFalse(optionalU8.isValidInstance(listOf(1)))

        assertFalse(optionalBoolean.isValidInstance(1))
        assertFalse(optionalU8.isValidInstance(1))
    }
}