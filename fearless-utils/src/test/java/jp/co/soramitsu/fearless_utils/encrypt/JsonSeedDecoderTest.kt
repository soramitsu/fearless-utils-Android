package jp.co.soramitsu.fearless_utils.encrypt

import com.google.gson.Gson
import jp.co.soramitsu.fearless_utils.encrypt.JsonSeedDecodingException.InvalidJsonException
import jp.co.soramitsu.fearless_utils.encrypt.JsonSeedDecodingException.IncorrectPasswordException
import jp.co.soramitsu.fearless_utils.ss58.AddressType
import jp.co.soramitsu.fearless_utils.ss58.SS58Encoder
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

private const val VALID_JSON = "{\"address\":\"F2dMuaCik4Ackmo9hoMMV79ETtVNvKSZMVK5sue9q1syPrW\",\"encoded\":\"DjQJTO2m1HlbCuaF0A9B9XJPHQlz1+0dOVURUSSS3VsAgAAAAQAAAAgAAAC9nLArVYH4ip7+fN03vcLOy727cNE6PWMCVXtpPKoAktb4YTIaf/Oe8oPZOUa1KnMCPtTRJPUsZbCMp41rdaT82b6wvOI/CL3kmmPlVIX8GmW6GZHaPVvvu2fGiC7EchpV5V1SR11GhTbqE94JhfSKSJALkd1Nxy4Ilfizyx7UZAw/pzavw7PR7td0MXEjctUhiWg1TDApXXGiUX3+\",\"encoding\":{\"content\":[\"pkcs8\",\"sr25519\"],\"type\":[\"scrypt\",\"xsalsa20-poly1305\"],\"version\":\"3\"},\"meta\":{\"genesisHash\":\"0xb0a8d493285c2df73290dfb7e61f870f17b41801197a149ca93654499ea3dafe\",\"name\":\"test\",\"tags\":[],\"whenCreated\":1600410143263}}"

private const val VALID_PASSWORD = "12345"
private const val INVALID_PASSWORD = "not_valid_pass"

private const val VALID_NAME = "name"
private const val VALID_ADDRESS = "F2dMuaCik4Ackmo9hoMMV79ETtVNvKSZMVK5sue9q1syPrW"
private val VALID_NETWORK = AddressType.KUSAMA


private const val INVALID_JSON = "\"some_field\": 123"

@RunWith(MockitoJUnitRunner::class)
class JsonSeedDecoderTest {
    private val gson = Gson()
    private val ss58 = SS58Encoder()
    private val keypairFactory = KeypairFactory()

    private var decoder = JsonSeedDecoder(gson, ss58, keypairFactory)

    @Test
    fun `should decode valid json with correct password`() {
        val result = decoder.decode(VALID_JSON, VALID_PASSWORD)

        assertEquals(VALID_NAME, result.username)
        assertEquals(VALID_NETWORK, result.networType)
        assertEquals(VALID_ADDRESS, result.address)
    }

    @Test(expected = IncorrectPasswordException::class)
    fun `should handle valid json with incorrect password`() {
        decoder.decode(VALID_JSON, INVALID_PASSWORD)
    }

    @Test(expected = InvalidJsonException::class)
    fun `should handle invalid json with valid password`() {
        decoder.decode(INVALID_JSON, VALID_PASSWORD)
    }

    @Test(expected = InvalidJsonException::class)
    fun `should handle invalid json with incorrect password`() {
        decoder.decode(INVALID_JSON, INVALID_PASSWORD)
    }
}