package jp.co.soramitsu.fearless_utils_android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType
import jp.co.soramitsu.fearless_utils.encrypt.Signer
import jp.co.soramitsu.fearless_utils.encrypt.json.JsonSeedDecoder
import jp.co.soramitsu.fearless_utils.encrypt.json.JsonSeedEncoder
import jp.co.soramitsu.fearless_utils.encrypt.keypair.substrate.Sr25519Keypair
import jp.co.soramitsu.fearless_utils.encrypt.keypair.substrate.SubstrateKeypairFactory
import org.spongycastle.util.encoders.Hex
import java.security.SecureRandom

private val SEED = Hex.decode("3132333435363738393031323334353637383930313233343536373839303132")
private val PASSWORD = "12345"
private val NAME = "name"

private const val ADDRESS_TYPE_WESTEND: Byte = 42
private const val GENESIS_HASH_WESTEND =
    "e143f23803ac50e8f6f8e62695d1ce9e4e1d68aa36c1cd2cfd15340213f3423e"

private val gson = Gson()

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        shouldSignMessage()

        shouldEncodeSr25519Json()
    }

    private fun shouldEncodeSr25519Json() {
        val keypairExpected = SubstrateKeypairFactory.generate(EncryptionType.SR25519, SEED)

        require(keypairExpected is Sr25519Keypair)

        val decoder = JsonSeedDecoder(gson)
        val encoder = JsonSeedEncoder(gson, SecureRandom())

        val json = encoder.generate(
            keypair = keypairExpected,
            seed = null,
            password = PASSWORD,
            name = NAME,
            encryptionType = EncryptionType.SR25519,
            addressByte = ADDRESS_TYPE_WESTEND,
            genesisHash = GENESIS_HASH_WESTEND
        )

        val decoded = decoder.decode(json, PASSWORD)

        with(decoded) {
            val keypair = keypair

            require(keypair is Sr25519Keypair)

            require(keypairExpected.publicKey.contentEquals(keypair.publicKey))
            require(keypairExpected.privateKey.contentEquals(keypair.privateKey))
            require(keypairExpected.nonce.contentEquals(keypair.nonce))
            require(NAME == username)
            require(seed == null)
        }
    }

    private fun shouldSignMessage() {
        val messageHex = "this is a message"

        val keypair = SubstrateKeypairFactory.generate(EncryptionType.SR25519, SEED)

        val result = Signer.sign(EncryptionType.SR25519, messageHex.toByteArray(), keypair)

        require(
            Signer.verifySr25519(
                messageHex.toByteArray(),
                result.signature,
                keypair.publicKey
            )
        )
    }
}