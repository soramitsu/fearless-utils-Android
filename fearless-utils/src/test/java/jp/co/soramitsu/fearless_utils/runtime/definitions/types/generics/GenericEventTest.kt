package jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics

import jp.co.soramitsu.fearless_utils.common.assertThrows
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.BaseTypeTest
import jp.co.soramitsu.schema.definitions.types.errors.EncodeDecodeException
import jp.co.soramitsu.schema.definitions.types.fromHex
import jp.co.soramitsu.schema.definitions.types.toHex
import org.junit.Assert.*
import org.junit.Test

class GenericEventTest : BaseTypeTest() {

    val inHex = "0x01000103"

    val instance = GenericEvent.Instance(
        moduleIndex = 1,
        eventIndex = 0,
        arguments = listOf(
            true,
            3.toBigInteger()
        )
    )

    @Test
    fun `should encode correct event`() {
        val encoded = GenericEvent(runtime.metadata).toHex(instance)

        assertEquals(inHex, encoded)
    }

    @Test
    fun `should decode correct call`() {
        val decoded = GenericEvent(runtime.metadata).fromHex(inHex)

        assertEquals(instance.arguments, decoded.arguments)
        assertEquals(instance.moduleIndex, decoded.moduleIndex)
        assertEquals(instance.eventIndex, decoded.eventIndex)
    }

    @Test
    fun `should throw for encoding instance with invalid index`() {
        val invalidInstance = GenericEvent.Instance(
            moduleIndex = 2,
            eventIndex = 3,
            arguments = emptyList()
        )

        assertThrows<EncodeDecodeException> { GenericEvent(runtime.metadata).toHex(invalidInstance) }
    }

    @Test
    fun `should throw for encoding instance with invalid arguments`() {
        val invalidInstance = GenericEvent.Instance(
            moduleIndex = 1,
            eventIndex = 0,
            arguments = listOf(
                "arg1" to true,
                "arg2" to 3 // invalid param type - should be BigInteger
            )
        )

        assertThrows<EncodeDecodeException> { GenericEvent(runtime.metadata).toHex(invalidInstance) }
    }

    @Test
    fun `should throw if decoding instance with invalid index`() {
        val inHex = "0x0203"

        assertThrows<EncodeDecodeException> { GenericEvent(runtime.metadata).fromHex(inHex) }
    }

    @Test
    fun `should throw if decoding instance with invalid arguments`() {
        val inHex = "0x01000412"

        assertThrows<EncodeDecodeException> { GenericEvent(runtime.metadata).fromHex(inHex) }
    }

    @Test
    fun `should validate instance`() {
        assertTrue(GenericEvent(runtime.metadata).isValidInstance(instance))

        assertFalse(GenericEvent(runtime.metadata).isValidInstance(1))
    }
}
