package jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics

import jp.co.soramitsu.fearless_utils.common.assertThrows
import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.errors.EncodeDecodeException
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.fromHex
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.toHex
import jp.co.soramitsu.fearless_utils.runtime.metadata.ExtrinsicMetadata
import jp.co.soramitsu.fearless_utils.runtime.metadata.RuntimeMetadata
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.math.BigInteger

@RunWith(MockitoJUnitRunner::class)
class SignedExtrasTest {

    @Mock
    lateinit var runtime: RuntimeSnapshot

    @Mock
    lateinit var metadata: RuntimeMetadata

    @Mock
    lateinit var extrinsicMetadata: ExtrinsicMetadata

    @Before
    fun setup() {
        `when`(runtime.metadata).thenReturn(metadata)
        `when`(metadata.extrinsic).thenReturn(extrinsicMetadata)
    }

    @Test
    fun `should encode full set of extras`() {
        extrinsicContainsExtras(SignedExtras.ERA, SignedExtras.NONCE, SignedExtras.TIP)

        val extras = mapOf(
            SignedExtras.TIP to BigInteger.ONE,
            SignedExtras.NONCE to BigInteger.TEN,
            SignedExtras.ERA to Era.Immortal
        )

        val encoded = SignedExtras.toHex(runtime, extras)

        assertEquals("0x002804", encoded)
    }

    @Test
    fun `should encode partial set of extras and ignore unused arguments`() {
        extrinsicContainsExtras(SignedExtras.NONCE, SignedExtras.TIP)

        val extras = mapOf(
            SignedExtras.TIP to BigInteger.ONE,
            SignedExtras.NONCE to BigInteger.TEN,
            SignedExtras.ERA to Era.Immortal // CheckMortality is unused
        )

        val encoded = SignedExtras.toHex(runtime, extras)

        assertEquals("0x2804", encoded)
    }

    @Test
    fun `should encode empty set of extras and ignore unused arguments`() {
        extrinsicContainsExtras()

        // all are unused
        val extras = mapOf(
            SignedExtras.TIP to BigInteger.ONE,
            SignedExtras.NONCE to BigInteger.TEN,
            SignedExtras.ERA to Era.Immortal
        )

        val encoded = SignedExtras.toHex(runtime, extras)

        assertEquals("0x", encoded)
    }

    @Test
    fun `should require used extras`() {
        extrinsicContainsExtras(SignedExtras.NONCE, SignedExtras.TIP)

        val extras = mapOf(
            SignedExtras.TIP to BigInteger.ONE,
        )

        assertThrows<EncodeDecodeException> {
            SignedExtras.toHex(runtime, extras)
        }
    }

    @Test
    fun `should decode full set of extras`() {
        extrinsicContainsExtras(SignedExtras.ERA, SignedExtras.NONCE, SignedExtras.TIP)

        val inHex = "0x002804"

        val decoded = SignedExtras.fromHex(runtime, inHex)

        assertEquals(decoded.size, 3)
    }

    @Test
    fun `should decode partial set of extras`() {
        extrinsicContainsExtras(SignedExtras.NONCE, SignedExtras.TIP)

        val inHex = "0x2804"

        val decoded = SignedExtras.fromHex(runtime, inHex)

        assertEquals(decoded.size, 2)
    }

    @Test
    fun `should decode empty set of extras`() {
        extrinsicContainsExtras()

        val inHex = "0x"

        val decoded = SignedExtras.fromHex(runtime, inHex)

        assertEquals(decoded.size, 0)
    }

    private fun extrinsicContainsExtras(vararg extras: String) {
        `when`(extrinsicMetadata.signedExtensions).thenReturn(extras.toList())
    }
}