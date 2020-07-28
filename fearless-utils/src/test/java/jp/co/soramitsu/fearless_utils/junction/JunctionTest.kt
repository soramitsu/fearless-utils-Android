package jp.co.soramitsu.fearless_utils.junction

import org.junit.Assert.*
import org.junit.Test

import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.spongycastle.util.encoders.Hex

@RunWith(MockitoJUnitRunner::class)
class JunctionTest {

    private lateinit var junctionDecoder: JunctionDecoder

    @Before
    fun setUp() {
        junctionDecoder = JunctionDecoder()
    }

    @Test
    fun decodeDerivationPath_called() {
        val path1 = "/1"
        val result1 = mutableListOf(Junction(JunctionType.SOFT, Hex.decode("0100000000000000000000000000000000000000000000000000000000000000")))
        val path2 = "//2"
        val result2 = mutableListOf(Junction(JunctionType.HARD, Hex.decode("0200000000000000000000000000000000000000000000000000000000000000")))
        val path3 = "//2/3"
        val result3 = mutableListOf(Junction(JunctionType.HARD, Hex.decode("0200000000000000000000000000000000000000000000000000000000000000")), Junction(JunctionType.SOFT, Hex.decode("0300000000000000000000000000000000000000000000000000000000000000")))

        assertEquals(result1.toString(), junctionDecoder.decodeDerivationPath(path1).toString())
        assertEquals(result2.toString(), junctionDecoder.decodeDerivationPath(path2).toString())
        assertEquals(result3.toString(), junctionDecoder.decodeDerivationPath(path3).toString())
    }
}