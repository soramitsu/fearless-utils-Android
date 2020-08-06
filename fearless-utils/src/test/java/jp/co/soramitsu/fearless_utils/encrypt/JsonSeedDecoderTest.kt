package jp.co.soramitsu.fearless_utils.encrypt

import com.google.gson.Gson
import io.github.novacrypto.bip39.Words
import jp.co.soramitsu.fearless_utils.encrypt.xsalsa20poly1305.SecretBox
import jp.co.soramitsu.fearless_utils.encrypt.xsalsa20poly1305.SimpleBox
import org.bouncycastle.crypto.generators.SCrypt
import org.bouncycastle.jcajce.provider.digest.Blake2b
import org.junit.Test

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.spongycastle.util.encoders.Base64
import org.spongycastle.util.encoders.Hex
import java.nio.ByteBuffer
import java.nio.ByteOrder

@RunWith(MockitoJUnitRunner::class)
class JsonSeedDecoderTest {

    private lateinit var jsonSeedDecoder: JsonSeedDecoder

    @Before
    fun setUp() {
        jsonSeedDecoder = JsonSeedDecoder(Gson())
    }

    @Test
    fun generateEntropy() {
        val json = "{\"address\":\"5DxxqfFGNftnAqG5QL3mQuuPcNqmHHGe5JeYjUQ25LFTEfQU\",\"encoded\":\"Z6xho/uvnd0i+MaZHV926ocE0ey+pzM++imnGNpbEO0AgAAAAQAAAAgAAABxaMRL3yv2agBKghYAat/upGB8+xDO4dQYNKg6mPlYcdJL+s21RwJlhapN+k7sgbfadeCi1ii7RITKJ56xJEbVhJHkEzpwBsLOg+JT/uFRSHtlFNjfkyLVk03U+AjrBHBngulV/uIxQJu4CndJRKrkOPz0wOkTEMQzCqAo5H9gSLO9o/mPqEB3kXxax35Ugyliq5XGWjUh6jABt7YO\",\"encoding\":{\"content\":[\"pkcs8\",\"sr25519\"],\"type\":[\"scrypt\",\"xsalsa20-poly1305\"],\"version\":\"3\"},\"meta\":{\"genesisHash\":\"0xe143f23803ac50e8f6f8e62695d1ce9e4e1d68aa36c1cd2cfd15340213f3423e\",\"name\":\"testing\",\"tags\":[],\"whenCreated\":1596621182533}}"
        val seed = "b2c5dccbdf19fb52a889de3be2a911fee5a6ac600a6ce80686cc34308a5ac7d0"
        println(Hex.toHexString(Sr25519.keypairFromSeed(Hex.decode(seed))))

        val jsonDec = jsonSeedDecoder.decode(json)
        val byteData = Base64.decode(jsonDec.encoded)
//        <scrypt salt 32 bytes> + <scrypt N 4 bytes le number> + <scrypt p 4 bytes le number> + <scrypt r 4 bytes le number> + <salsa nonce 24 bytes> + <remaining encrypted data>
        val salt = byteData.copyOfRange(0, 32)
        val N = ByteBuffer.wrap(byteData.copyOfRange(32, 36)).order(ByteOrder.LITTLE_ENDIAN).int
        val p = ByteBuffer.wrap(byteData.copyOfRange(36, 40)).order(ByteOrder.LITTLE_ENDIAN).int
        val r = ByteBuffer.wrap(byteData.copyOfRange(40, 44)).order(ByteOrder.LITTLE_ENDIAN).int

        val nonce = byteData.copyOfRange(44, 68)
        val encrData = byteData.copyOfRange(68, byteData.size)
//        @param P     the bytes of the pass phrase.
//        * @param S     the salt to use for this invocation.
//        * @param N     CPU/Memory cost parameter. Must be larger than 1, a power of 2 and less than
//        *              <code>2^(128 * r / 8)</code>.
//        * @param r     the block size, must be &gt;= 1.
//        * @param p     Parallelization parameter. Must be a positive integer less than or equal to
//        *              <code>Integer.MAX_VALUE / (128 * r * 8)</code>.
//        * @param dkLen the length of the key to generate.
        val d = SCrypt.generate("testingpassword".toByteArray(Charsets.UTF_8), salt, N, r, p, 32)
        val a = SecretBox(d).open(nonce, encrData).get()

    }
}