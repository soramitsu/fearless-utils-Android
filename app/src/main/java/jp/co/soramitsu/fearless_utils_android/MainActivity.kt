package jp.co.soramitsu.fearless_utils_android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import jp.co.soramitsu.fearless_utils.bip39.Bip39
import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType
import jp.co.soramitsu.fearless_utils.encrypt.KeypairFactory
import jp.co.soramitsu.fearless_utils.junction.JunctionDecoder
import jp.co.soramitsu.fearless_utils.ss58.AddressType
import jp.co.soramitsu.fearless_utils.ss58.SS58Encoder
import org.spongycastle.util.encoders.Hex

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val keypairFactory = KeypairFactory()
        val bip39 = Bip39()
        val junDecoder = JunctionDecoder()

        val key = "0x345071da55e5dccefaaa440339415ef9f2663338a38f7da0df21be5ab4e055ef"
//        302a300506032b6570032100345071da55e5dccefaaa440339415ef9f2663338a38f7da0df21be5ab4e055ef
//        302a300506032b6570032100e74d2cc3000fb7babda744be438cc7f8f1fc2914ffe876a8cf6721d2d5d47ed6
//        302e020100300506032b657004220420f37f55b4b0e1aca280c64a19e787c5151ed3a1d9b02bc0da0f594a599b9bc429
        val address = "5DFJF7tY4bpbpcKPJcBTQaKuCDEPCpiz8TRjpmLeTtweqmXL"

        val mnemonic = "bottom drive obey lake curtain smoke basket hold race lonely fit walk"
        val path = "///password"

        val password = junDecoder.getPassword(path)
        val entropy = bip39.generateEntropy(mnemonic)
        val seed = bip39.generateSeed(entropy, password)
        val sS58Encoder = SS58Encoder()

        val keypair = keypairFactory.generate(EncryptionType.ECDSA, seed, path)
        val addr = sS58Encoder.encode(keypair.publicKey, AddressType.WESTEND)
        println("lal1: ${Hex.toHexString(keypair.publicKey)}")
        println("lal2: ${addr}")


    }
}