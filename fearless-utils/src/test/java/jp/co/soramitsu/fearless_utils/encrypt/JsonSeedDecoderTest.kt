package jp.co.soramitsu.fearless_utils.encrypt

import com.google.gson.Gson
import io.github.novacrypto.bip39.Words
import jp.co.soramitsu.fearless_utils.encrypt.xsalsa20poly1305.SimpleBox
import org.bouncycastle.jcajce.provider.digest.Blake2b
import org.junit.Test

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.spongycastle.util.encoders.Base64
import org.spongycastle.util.encoders.Hex

@RunWith(MockitoJUnitRunner::class)
class JsonSeedDecoderTest {

    private lateinit var jsonSeedDecoder: JsonSeedDecoder

    @Before
    fun setUp() {
        jsonSeedDecoder = JsonSeedDecoder(Gson())
    }

    @Test
    fun generateEntropy() {
        val json =
            "{\"address\":\"5H9LYLQeDup7xpPfi33QTb6GVWNMrVQqVzL5xAdes234yjGc\",\"encoded\":\"f1GxtyIWfZnI+3VPL6N9svsmzZllJrznlV0+lT5RVjkAgAAAAQAAAAgAAAAnNufihFaiWSi6rv2+OpHPRhIg/CWEWUWBmEvPBhkkvDo4qsT8peODd6hMNAxDMm9cfXIGsK3GcWMCz5WT37pZuRirjZlayVe0gnNoPkaPnsGhy77glF/JO4Ck/+LyCwAiVNU+KAb0L1izJTqdevDwa+8ldv/DYtwKqi2wsllPd/XkKQ57xm9wtI760RRHgThJsvg4VOaKBKVHnpED\",\"encoding\":{\"content\":[\"pkcs8\",\"sr25519\"],\"type\":[\"scrypt\",\"xsalsa20-poly1305\"],\"version\":\"3\"},\"meta\":{\"genesisHash\":\"0xe143f23803ac50e8f6f8e62695d1ce9e4e1d68aa36c1cd2cfd15340213f3423e\",\"name\":\"mrzizik2\",\"tags\":[],\"whenCreated\":1596144767403}}"
        val seed = "a0a0323f0b48b569004c2709d8e65fafe26294706beac3a36fb6102b353accf4"

        val jsonDec = jsonSeedDecoder.decode(json)

        val key = Blake2b.Blake2b256().digest("k11irdi".toByteArray())

        println(seed)
        println(Hex.toHexString(SimpleBox(key).seal(Base64.decode(jsonDec.encoded))))
    }
}