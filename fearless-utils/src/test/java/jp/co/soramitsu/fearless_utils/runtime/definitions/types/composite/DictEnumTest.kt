package jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite

import jp.co.soramitsu.fearless_utils.runtime.definitions.types.BaseTypeTest
import jp.co.soramitsu.schema.definitions.types.TypeReference
import jp.co.soramitsu.schema.definitions.types.composite.DictEnum
import jp.co.soramitsu.schema.definitions.types.fromHex
import jp.co.soramitsu.schema.definitions.types.primitives.BooleanType
import jp.co.soramitsu.schema.definitions.types.primitives.u128
import jp.co.soramitsu.schema.definitions.types.primitives.u8
import jp.co.soramitsu.schema.definitions.types.toHex
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


@RunWith(JUnit4::class)
class DictEnumTest : BaseTypeTest() {

    private val enumValues = listOf(
        DictEnum.Entry("A", TypeReference(u8)),
        DictEnum.Entry("B", TypeReference(BooleanType)),
        DictEnum.Entry("C", TypeReference(u128))
    )

    private val type = DictEnum(
        "test",
        enumValues,
    )

    @Test
    fun `should decode instance`() {
        val expectedInstance = DictEnum.Entry("B", true)
        val inHex = "0x0101"

        val decoded = type.fromHex(inHex)

        assertEquals(expectedInstance.name, decoded.name)
        assertEquals(expectedInstance.value, decoded.value)
    }

    @Test
    fun `should encode instance`() {
        val instance = DictEnum.Entry("A", 1.toBigInteger())

        val encoded = type.toHex(instance)

        assertEquals("0x0001", encoded)
    }

    @Test
    fun `should validate instance`() {
        assertTrue(type.isValidInstance(DictEnum.Entry("B", false)))

        assertFalse(type.isValidInstance(DictEnum.Entry("F", 1)))
        assertFalse(type.isValidInstance(DictEnum.Entry("A", "Not a Bigint")))

        assertFalse(type.isValidInstance(1))
    }
}
