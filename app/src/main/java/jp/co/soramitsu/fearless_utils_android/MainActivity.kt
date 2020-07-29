package jp.co.soramitsu.fearless_utils_android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import jp.co.soramitsu.fearless_utils.bip39.Bip39
import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType
import jp.co.soramitsu.fearless_utils.encrypt.KeypairFactory
import jp.co.soramitsu.fearless_utils.encrypt.Signer
import org.spongycastle.util.encoders.Hex

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bip39 = Bip39()
        val seed = Hex.decode("44e9d125f037ac1d51f0a7d3649689d422c2af8b1ec8e00d71db4d7bf6d127e3")

        val keypairFac = KeypairFactory()
        val keypair = keypairFac.generate(EncryptionType.ECDCA, seed, "")
        val sign = Signer()
        val message = "message".toByteArray()
        sign.sign(EncryptionType.ECDCA, message, keypair)
    }
}