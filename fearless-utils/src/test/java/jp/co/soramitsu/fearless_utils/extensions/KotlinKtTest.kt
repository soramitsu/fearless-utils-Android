package jp.co.soramitsu.fearless_utils.extensions

import org.junit.Assert.*
import org.junit.Test
import java.math.BigInteger
import java.nio.ByteOrder

class KotlinKtTest {

    @Test
    fun `should create little endian big int`() {
        val bytes = jp.co.soramitsu.schema.extensions.fromHex()

        assertEquals(BigInteger("1097877634998"),
            jp.co.soramitsu.schema.extensions.toBigInteger(ByteOrder.LITTLE_ENDIAN)
        )
    }
}