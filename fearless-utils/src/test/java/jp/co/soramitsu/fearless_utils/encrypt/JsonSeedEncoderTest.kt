package jp.co.soramitsu.fearless_utils.encrypt

import com.google.gson.Gson
import jp.co.soramitsu.fearless_utils.encrypt.json.JsonSeedDecoder
import jp.co.soramitsu.fearless_utils.encrypt.json.JsonSeedEncoder
import jp.co.soramitsu.fearless_utils.ss58.AddressType
import jp.co.soramitsu.fearless_utils.ss58.SS58Encoder
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.security.SecureRandom

private const val PASSWORD = "12345"
private const val NAME = "test"

@RunWith(MockitoJUnitRunner::class)
class JsonSeedEncoderTest {
    private val gson = Gson()
    private val ss58 = SS58Encoder()
    private val keypairFactory = KeypairFactory()

    private val decoder = JsonSeedDecoder(
        gson,
        ss58,
        keypairFactory
    )

    private val encoder = JsonSeedEncoder(gson, ss58, SecureRandom())

    @Test
    fun `should encode ed25519`() {
       performTest(EncryptionType.ED25519)
    }

    @Test
    fun `should encode ecdsa`() {
        performTest(EncryptionType.ECDSA)
    }

    @Test
    @Ignore("sr25519 is not supported in unit tests")
    fun `should encode sr25519`() {
        performTest(EncryptionType.SR25519)
    }

    private fun performTest(encryptionType: EncryptionType) {
        val seedExpected = TestData.SEED_BYTES
        val keypairExpected = keypairFactory.generate(encryptionType, seedExpected)

        val json = encoder.generate(
            keypair = keypairExpected,
            seed = seedExpected,
            password = PASSWORD,
            name = NAME,
            encryptionType = encryptionType,
            addressType = AddressType.WESTEND
        )

        val decoded = decoder.decode(json, PASSWORD)

        with(decoded) {
            assert(keypairExpected.publicKey.contentEquals(keypair.publicKey))
            assert(keypairExpected.privateKey.contentEquals(keypair.privateKey))
            assertEquals(NAME, username)
            assertNotNull(seed)
            assert(seedExpected.contentEquals(seed!!))
        }
    }
}