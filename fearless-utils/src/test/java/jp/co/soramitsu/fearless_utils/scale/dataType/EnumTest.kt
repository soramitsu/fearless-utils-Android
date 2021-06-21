package jp.co.soramitsu.fearless_utils.scale.dataType

import jp.co.soramitsu.schema.scale.Schema
import jp.co.soramitsu.schema.scale.enum
import jp.co.soramitsu.schema.scale.toHexString
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

enum class TestEnum {
    ZERO, ONE, TWO
}

object EnumTypeTest : Schema<EnumTypeTest>() {
    val enumField by enum(TestEnum::class)
}

@RunWith(MockitoJUnitRunner::class)
class EnumTest {

    @Test
    fun `should serialize an deserialize enum`() {
        val hex = "0x01"

        val parsed = EnumTypeTest.read(hex)

        assertEquals(parsed[EnumTypeTest.enumField], TestEnum.ONE)

        val afterIo = parsed.toHexString()

        assertEquals(hex, afterIo)
    }
}
