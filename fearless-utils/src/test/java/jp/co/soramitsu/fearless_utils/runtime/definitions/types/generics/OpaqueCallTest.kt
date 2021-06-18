package jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics

import jp.co.soramitsu.fearless_utils.runtime.definitions.types.BaseTypeTest
import jp.co.soramitsu.schema.definitions.types.fromHex
import jp.co.soramitsu.schema.definitions.types.toHex
import org.junit.Assert.assertEquals
import org.junit.Test

class OpaqueCallTest : BaseTypeTest() {
    val inHex = "0x1001000103"

    val instance = GenericCall.Instance(
        moduleIndex = 1,
        callIndex = 0,
        arguments = mapOf(
            "arg1" to true,
            "arg2" to 3.toBigInteger()
        )
    )

    @Test
    fun `should decode call`() {
        val decoded = OpaqueCall(runtime.metadata).fromHex(inHex)

        assertEquals(instance.arguments, decoded.arguments)
        assertEquals(instance.moduleIndex, decoded.moduleIndex)
        assertEquals(instance.callIndex, decoded.callIndex)
    }

    @Test
    fun `should encode call`() {
        val encoded = OpaqueCall(runtime.metadata).toHex(instance)

        assertEquals(inHex, encoded)
    }
}
