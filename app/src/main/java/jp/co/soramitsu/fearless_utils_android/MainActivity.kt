package jp.co.soramitsu.fearless_utils_android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import jp.co.soramitsu.fearless_utils.encrypt.JsonSeedDecoder
import jp.co.soramitsu.fearless_utils.encrypt.KeypairFactory
import jp.co.soramitsu.fearless_utils.ss58.AddressType
import jp.co.soramitsu.fearless_utils.ss58.SS58Encoder
import org.junit.Assert

private const val VALID_JSON = "{\"address\":\"F2dMuaCik4Ackmo9hoMMV79ETtVNvKSZMVK5sue9q1syPrW\",\"encoded\":\"rcKVUQuq9OXJz5peaK+WiIfkA3WUq9UQyKFEWHDpnfwAgAAAAQAAAAgAAABVl8k8fX0YBTaKrDLC5wM/VBlHLTnyh5LGy4rnOmJ+0dA/adbk9N7hRW/vfzKjRZ3niFId5Grp6rnn5tfLrSkaJElB86E4LnB3M0tadwIJA4xd1JKrjCCZrrGVu18AQ2/LhZZBeEwyMYnW+UszWcEMTPxGfi6DrKYjUGYfhZ+ylrvsPm2J1lTi4fa976cWoO9WO9s2iS0OkFNVLqwK\",\"encoding\":{\"content\":[\"pkcs8\",\"sr25519\"],\"type\":[\"scrypt\",\"xsalsa20-poly1305\"],\"version\":\"3\"},\"meta\":{\"genesisHash\":\"0xb0a8d493285c2df73290dfb7e61f870f17b41801197a149ca93654499ea3dafe\",\"name\":\"test\",\"tags\":[],\"whenCreated\":1600410143263,\"whenEdited\":1600426009049}}"

private const val VALID_PASSWORD = "12345"
private const val INVALID_PASSWORD = "123456"

private const val VALID_NAME = "name"
private const val VALID_ADDRESS = "F2dMuaCik4Ackmo9hoMMV79ETtVNvKSZMVK5sue9q1syPrW"
private val VALID_NETWORK = AddressType.KUSAMA

private const val INVALID_JSON = "{\"some_field\": 123}"


class MainActivity : AppCompatActivity() {
    private val gson = Gson()
    private val ss58 = SS58Encoder()
    private val keypairFactory = KeypairFactory()

    private var decoder = JsonSeedDecoder(gson, ss58, keypairFactory)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val result = decoder.decode(VALID_JSON, VALID_PASSWORD)

        Assert.assertEquals(VALID_NAME, result.username)
        Assert.assertEquals(VALID_NETWORK, result.networType)
        Assert.assertEquals(VALID_ADDRESS, result.address)
    }
}