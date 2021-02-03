package jp.co.soramitsu.fearless_utils.extensions

import org.junit.Assert.*
import org.junit.Test
import java.math.BigInteger
import java.nio.ByteOrder

class KotlinKtTest {

    @Test
    fun `should create little endian big int`() {
        val bytes = "0xb63f9b9eff0000000000000000000000".fromHex()

        assertEquals(BigInteger("1097877634998"), bytes.toBigInteger(ByteOrder.LITTLE_ENDIAN))
    }
}