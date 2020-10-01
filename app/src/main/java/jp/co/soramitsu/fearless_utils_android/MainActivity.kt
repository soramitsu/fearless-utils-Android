package jp.co.soramitsu.fearless_utils_android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType
import jp.co.soramitsu.fearless_utils.encrypt.KeypairFactory
import jp.co.soramitsu.fearless_utils.encrypt.Signer
import org.spongycastle.util.encoders.Hex

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        shouldSignMessage()
    }

    private fun shouldSignMessage() {
        val messageHex = "this is a message"
        val seed = Hex.decode("3132333435363738393031323334353637383930313233343536373839303132")

        val keypair = KeypairFactory().generate(EncryptionType.SR25519, seed, "")

        val signer = Signer()

        val result = signer.sign(EncryptionType.SR25519, messageHex.toByteArray(), keypair)

        require(signer.verifySr25519(messageHex.toByteArray(), result.signature!!, keypair.publicKey))
    }
}