package jp.co.soramitsu.fearless_utils.junction

import org.junit.Test

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.spongycastle.util.encoders.Hex

@RunWith(MockitoJUnitRunner::class)
class JunctionTest {

    private val inputPublicKey = "6addccf0b805e2d0dc445239b800201e1fb6f17f92ef4eaa1516f4d0e2cf1664"
    private val outputAddress = "EzSUv17LNHTU2xdPKLuLkPy7fCD795DZ6d5CnF4x4HSkcb4"

    private lateinit var junctionDecoder: JunctionDecoder

    @Before
    fun setUp() {
        junctionDecoder = JunctionDecoder()
    }

    @Test
    fun decode_called() {
        val path = "//fearless/1337/kusama//polkadot///soramitsu"

        println(junctionDecoder.decodeDerivationPath(path))
    }
}