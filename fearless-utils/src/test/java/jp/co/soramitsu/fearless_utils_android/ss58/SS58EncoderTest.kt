package jp.co.soramitsu.fearless_utils_android.ss58

import org.junit.Test

import org.junit.Assert.assertEquals
import org.spongycastle.util.encoders.Hex

class SS58EncoderTest {
    private val inputPublicKey = "6addccf0b805e2d0dc445239b800201e1fb6f17f92ef4eaa1516f4d0e2cf1664"
    private val outputAddress = "EzSUv17LNHTU2xdPKLuLkPy7fCD795DZ6d5CnF4x4HSkcb4"

    private val ss58Encoder = SS58Encoder()

    @Test
    fun encode_called() {
        val result = ss58Encoder.encode(Hex.decode(inputPublicKey), AddressType.KUSAMA)

        assertEquals(outputAddress, result)
    }

    @Test
    fun decode_called() {
        val result = Hex.toHexString(ss58Encoder.decode(outputAddress, AddressType.KUSAMA))

        assertEquals(inputPublicKey, result)
    }
}