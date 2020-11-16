package jp.co.soramitsu.fearless_utils.encrypt

import com.google.gson.Gson
import jp.co.soramitsu.fearless_utils.encrypt.json.JsonSeedDecoder
import jp.co.soramitsu.fearless_utils.encrypt.json.JsonSeedDecodingException.IncorrectPasswordException
import jp.co.soramitsu.fearless_utils.encrypt.json.JsonSeedDecodingException.InvalidJsonException
import jp.co.soramitsu.fearless_utils.ss58.AddressType
import jp.co.soramitsu.fearless_utils.ss58.SS58Encoder
import org.junit.Assert.*
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

private const val VALID_JSON_SR25519 = "{\"address\":\"F2dMuaCik4Ackmo9hoMMV79ETtVNvKSZMVK5sue9q1syPrW\",\"encoded\":\"DjQJTO2m1HlbCuaF0A9B9XJPHQlz1+0dOVURUSSS3VsAgAAAAQAAAAgAAAC9nLArVYH4ip7+fN03vcLOy727cNE6PWMCVXtpPKoAktb4YTIaf/Oe8oPZOUa1KnMCPtTRJPUsZbCMp41rdaT82b6wvOI/CL3kmmPlVIX8GmW6GZHaPVvvu2fGiC7EchpV5V1SR11GhTbqE94JhfSKSJALkd1Nxy4Ilfizyx7UZAw/pzavw7PR7td0MXEjctUhiWg1TDApXXGiUX3+\",\"encoding\":{\"content\":[\"pkcs8\",\"sr25519\"],\"type\":[\"scrypt\",\"xsalsa20-poly1305\"],\"version\":\"3\"},\"meta\":{\"genesisHash\":\"0xb0a8d493285c2df73290dfb7e61f870f17b41801197a149ca93654499ea3dafe\",\"name\":\"test\",\"tags\":[],\"whenCreated\":1600410143263}}"
private const val VALID_JSON_EDWARDS = "{\"address\":\"GnSs2bd76KkrcvN7prrFfkbu939mkZHyBzVKnjS8ikg3CZ8\",\"encoded\":\"fPeWwZNEDSNKM+k+Vjv7HFGr/r7LSev6DHmX+5TwvqYAgAAAAQAAAAgAAAAzuSqaguZBu/q23iphXVSsn3GP70l4Pn/bTw6TdYltaMZDJRADWz+T7FUfZ460FkI70yczt9O1HSAEEj5QLMJEvxa4+wNQWA+hXeQoVxKf+1iUIr+RW9wL9JGpZw61HWhpr0t4b+TEBQwbf7hDT/P0dFmYLDa3RfxVg07GTrzYe2uBQt9MCYLUcctBNk7W7r8CI3FuqbAA/oP07GB/\",\"encoding\":{\"content\":[\"pkcs8\",\"ed25519\"],\"type\":[\"scrypt\",\"xsalsa20-poly1305\"],\"version\":\"3\"},\"meta\":{\"genesisHash\":\"0xb0a8d493285c2df73290dfb7e61f870f17b41801197a149ca93654499ea3dafe\",\"name\":\"test\",\"tags\":[],\"whenCreated\":1600432656361,\"whenEdited\":1600433019129}}"
private const val VALID_JSON_ECDSA = "{\"address\":\"0x02f3d42516c317757748b073f5221455e31035286ea7417b827d8eb8ad1a6c49d6\",\"encoded\":\"VsYSlEzIMvgk2lpGigHs8fR6kqyTZAgnz+QkoAwxy/0AgAAAAQAAAAgAAADtvqAkZhcSD94AaYzzDFLgMDgz0U3+ZD6p0eaWqlWXgPJZrTLe6Go6bXgiT0nklIHMipQ4CxDsnJwIO98NY7RJwWDqnH9+72huUq7VODaN7LUyChBLT58AwN0xXvx1cEksMlBZoCZ9W0qU0o98kfhPuLA+goplCb/XLp5PEdE=\",\"encoding\":{\"content\":[\"pkcs8\",\"ecdsa\"],\"type\":[\"scrypt\",\"xsalsa20-poly1305\"],\"version\":\"3\"},\"meta\":{\"genesisHash\":\"0xb0a8d493285c2df73290dfb7e61f870f17b41801197a149ca93654499ea3dafe\",\"name\":\"test\",\"tags\":[],\"whenCreated\":1600433095690}}"

private const val JSON_NO_GENESIS = "{\"address\":\"GnSs2bd76KkrcvN7prrFfkbu939mkZHyBzVKnjS8ikg3CZ8\",\"encoded\":\"fPeWwZNEDSNKM+k+Vjv7HFGr/r7LSev6DHmX+5TwvqYAgAAAAQAAAAgAAAAzuSqaguZBu/q23iphXVSsn3GP70l4Pn/bTw6TdYltaMZDJRADWz+T7FUfZ460FkI70yczt9O1HSAEEj5QLMJEvxa4+wNQWA+hXeQoVxKf+1iUIr+RW9wL9JGpZw61HWhpr0t4b+TEBQwbf7hDT/P0dFmYLDa3RfxVg07GTrzYe2uBQt9MCYLUcctBNk7W7r8CI3FuqbAA/oP07GB/\",\"encoding\":{\"content\":[\"pkcs8\",\"ed25519\"],\"type\":[\"scrypt\",\"xsalsa20-poly1305\"],\"version\":\"3\"},\"meta\":{\"name\":\"test\",\"tags\":[],\"whenCreated\":1600432656361,\"whenEdited\":1600433019129}}"

// TODO not possible to use sr25519 library in unit tests for now
val JSONS = listOf(VALID_JSON_EDWARDS, VALID_JSON_ECDSA)

private const val VALID_PASSWORD = "12345"
private const val INVALID_PASSWORD = "123456"

private const val VALID_NAME = "test"

private val VALID_ADDRESSES = listOf(
    "GnSs2bd76KkrcvN7prrFfkbu939mkZHyBzVKnjS8ikg3CZ8",
    "D3PX7p2qee3P6CfhZNDUXhrD6YtPcprqPTXe4CdPfYzWAtr"
)

private val VALID_NETWORK = AddressType.KUSAMA

private const val INVALID_JSON = "{\"some_field\": 123}"

@RunWith(MockitoJUnitRunner::class)
class JsonSeedDecoderTest {
    private val gson = Gson()
    private val ss58 = SS58Encoder()
    private val keypairFactory = KeypairFactory()

    private val decoder = JsonSeedDecoder(
        gson,
        ss58,
        keypairFactory
    )

    @Test
    fun `should decode valid json with correct password`() {
        JSONS.forEachIndexed { index, json ->
            val result = decoder.decode(json, VALID_PASSWORD)

            assertEquals(VALID_NAME, result.username)
            assertEquals(VALID_NETWORK, result.networkType)
            assertEquals(VALID_ADDRESSES[index], result.address)
        }
    }

    @Test
    fun `should handle valid json with incorrect password`() {
        JSONS.forEach {
            assertThrows<IncorrectPasswordException> {
                decoder.decode(it, INVALID_PASSWORD)
            }
        }
    }

    @Test(expected = InvalidJsonException::class)
    fun `should handle invalid json with valid password`() {
        decoder.decode(INVALID_JSON, VALID_PASSWORD)
    }

    @Test(expected = InvalidJsonException::class)
    fun `should handle invalid json with incorrect password`() {
        decoder.decode(INVALID_JSON, INVALID_PASSWORD)
    }

    @Test
    fun `should extract meta from valid json`() {
        val data = decoder.extractImportMetaData(VALID_JSON_SR25519)

        assertEquals(EncryptionType.SR25519, data.encryptionType)
        assertEquals(VALID_NETWORK, data.networkType)
        assertEquals(VALID_NAME, data.name)
    }

    @Test
    fun `should handle meta from invalid json`() {
        assertThrows<InvalidJsonException> {
            decoder.extractImportMetaData(INVALID_JSON)
        }
    }

    @Test
    fun `should handle json with no genesis`() {
        val result = decoder.decode(JSON_NO_GENESIS, VALID_PASSWORD)

        assertEquals(VALID_NETWORK, result.networkType)
    }

    @Test
    fun `should handle json with no network info`() {
        val result = decoder.decode(JSON_NO_GENESIS, VALID_PASSWORD)

        assertEquals(VALID_NETWORK, result.networkType)
    }

    @Test
    fun `should extract seed from non-sr25519 crypto`() {
        JSONS.forEach {
            val result = decoder.decode(it, VALID_PASSWORD)

            assertNotNull(result.seed)
        }
    }

    @Test
    @Ignore("sr25519 is not supported in unit tests")
    fun `should not extract seed from sr25519 crypto`() {
        val result = decoder.decode(VALID_JSON_SR25519, VALID_PASSWORD)

        assertNull(result.seed)
    }

    private inline fun <reified T : Throwable> assertThrows(block: () -> Unit) {
        var throwable: Throwable? = null

        try {
            block()
        } catch (t: Throwable) {
            throwable = t
        }

        assertNotNull("No error was thrown", throwable)
        assertTrue("${T::class} expected, but ${throwable!!::class} thrown", throwable is T)
    }
}