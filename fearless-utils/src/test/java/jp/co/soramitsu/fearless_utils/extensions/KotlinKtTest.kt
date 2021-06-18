package jp.co.soramitsu.fearless_utils.extensions

import jp.co.soramitsu.schema.extensions.fromHex
import jp.co.soramitsu.schema.extensions.toBigInteger
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigInteger
import java.nio.ByteOrder

class KotlinKtTest {

    @Test
    fun `should create little endian big int`() {
        val bytes = "0xb63f9b9eff0000000000000000000000".fromHex()

        assertEquals(BigInteger("1097877634998"),
            assertEquals(BigInteger("1097877634998"), bytes.toBigInteger(ByteOrder.LITTLE_ENDIAN))
        )
    }
}
