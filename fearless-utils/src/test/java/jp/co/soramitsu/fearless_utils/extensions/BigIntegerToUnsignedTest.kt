package jp.co.soramitsu.fearless_utils.extensions

import org.junit.Assert.*
import org.junit.Test
import java.math.BigInteger
import java.nio.ByteOrder

class BigIntegerToUnsignedTest {

    @Test
    fun `should create little endian big int`() {
        val bytes = "0xb63f9b9eff0000000000000000000000".fromHex()

        assertEquals(BigInteger("1097877634998"), bytes.fromUnsignedBytes(ByteOrder.LITTLE_ENDIAN))
    }

    @Test
    fun `should create big endian big int without padding`() {
        val bytes = "0x86b358".fromHex()

        assertEquals(BigInteger("8827736"), bytes.fromUnsignedBytes(ByteOrder.BIG_ENDIAN))
    }

    @Test
    fun `should create big endian big int with padding`() {
        val bytes = "0x6a3f7f".fromHex()

        assertEquals(BigInteger("6963071"), bytes.fromUnsignedBytes(ByteOrder.BIG_ENDIAN))
    }
}