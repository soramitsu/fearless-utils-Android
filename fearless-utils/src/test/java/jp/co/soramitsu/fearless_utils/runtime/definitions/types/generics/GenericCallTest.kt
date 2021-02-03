package jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics

import jp.co.soramitsu.fearless_utils.common.assertThrows
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.BaseTypeTest
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.errors.EncodeDecodeException
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.fromHex
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.toHex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GenericCallTest : BaseTypeTest() {

    val inHex = "0x01000103"

    val instance = GenericCall.Instance(
        moduleIndex = 1,
        callIndex = 0,
        arguments = mapOf(
            "arg1" to true,
            "arg2" to 3.toBigInteger()
        )
    )

    @Test
    fun `should encode correct call`() {
        val encoded = GenericCall.toHex(runtime, instance)

        assertEquals(inHex, encoded)
    }

    @Test
    fun `should decode correct call`() {
        val decoded = GenericCall.fromHex(runtime, inHex)

        assertEquals(instance.arguments, decoded.arguments)
        assertEquals(instance.moduleIndex, decoded.moduleIndex)
        assertEquals(instance.callIndex, decoded.callIndex)
    }

    @Test
    fun `should throw for encoding instance with invalid index`() {
        val invalidInstance = GenericCall.Instance(
            moduleIndex = 2,
            callIndex = 3,
            arguments = emptyMap()
        )

        assertThrows<EncodeDecodeException> { GenericCall.toHex(runtime, invalidInstance) }
    }

    @Test
    fun `should throw for encoding instance with invalid arguments`() {
        val invalidInstance = GenericCall.Instance(
            moduleIndex = 1,
            callIndex = 0,
            arguments = mapOf(
                "arg1" to true,
                "arg2" to 3  // invalid param type - should be BigInteger
            )
        )

        assertThrows<EncodeDecodeException> { GenericCall.toHex(runtime, invalidInstance) }
    }

    @Test
    fun `should throw if decoding instance with invalid index`() {
        val inHex = "0x0203"

        assertThrows<EncodeDecodeException> { GenericCall.fromHex(runtime, inHex) }
    }

    @Test
    fun `should throw if decoding instance with invalid arguments`() {
        val inHex = "0x01000412"

        assertThrows<EncodeDecodeException> { GenericCall.fromHex(runtime, inHex) }
    }

    @Test
    fun `should validate instance`() {
        assertTrue(GenericCall.isValidInstance(instance))

        assertFalse(GenericCall.isValidInstance(1))
    }
}