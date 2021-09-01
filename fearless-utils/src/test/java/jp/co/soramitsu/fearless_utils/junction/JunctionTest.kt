package jp.co.soramitsu.fearless_utils.junction

import jp.co.soramitsu.fearless_utils.assertListEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import org.spongycastle.util.encoders.Hex

abstract class JunctionTest {

    protected abstract val decoder: JunctionDecoder

    protected fun performTest(
        path: String,
        expectedPassword: String?,
        vararg expectedJunctions: Junction
    ) {

        val decodeResult = decoder.decode(path)

        assertEquals(expectedPassword, decodeResult.password)

        assertListEquals(expectedJunctions.toList(), decodeResult.junctions, comparator = { expected, actual ->
            expected.chaincode.contentEquals(actual.chaincode) && expected.type == actual.type
        })
    }
}