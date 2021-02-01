package jp.co.soramitsu.fearless_utils.ss58

import jp.co.soramitsu.fearless_utils.common.TestAddressBytes
import jp.co.soramitsu.fearless_utils.extensions.fromHex
import jp.co.soramitsu.fearless_utils.extensions.toHexString
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SS58EncoderTest {

    private val PUBLIC_KEY = "6addccf0b805e2d0dc445239b800201e1fb6f17f92ef4eaa1516f4d0e2cf1664"
    private val KUSAMA_ADDRESS = "EzSUv17LNHTU2xdPKLuLkPy7fCD795DZ6d5CnF4x4HSkcb4"

    private lateinit var ss58Encoder: SS58Encoder

    @Before
    fun setUp() {
        ss58Encoder = SS58Encoder()
    }

    @Test
    fun `should encode address from public key`() {
        val result = ss58Encoder.encode(PUBLIC_KEY.fromHex(), TestAddressBytes.KUSAMA)

        assertEquals(KUSAMA_ADDRESS, result)
    }

    @Test
    fun `should decode public key from address`() {
        val result = ss58Encoder.decode(KUSAMA_ADDRESS).toHexString()

        assertEquals(PUBLIC_KEY, result)
    }
}