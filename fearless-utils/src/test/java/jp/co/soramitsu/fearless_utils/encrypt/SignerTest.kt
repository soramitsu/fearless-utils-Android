package jp.co.soramitsu.fearless_utils.encrypt

import jp.co.soramitsu.fearless_utils.bip39.Bip39
import jp.co.soramitsu.fearless_utils.bip39.MnemonicLength
import jp.co.soramitsu.fearless_utils.encrypt.model.Keypair
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.spongycastle.util.encoders.Hex

@RunWith(MockitoJUnitRunner::class)
class SignerTest {

    @Test
    fun `should sign message`() {
        val messageHex = "this is a message"

//        val expected = "90588f3f512496f2dd40571d162e8182860081c74e2085316e7c4396918f07da412ee029978e4dd714057fe973bd9e7d645148bf7b66680d67c93227cde95202"
//
//        val publicKey = "2f8c6129d816cf51c374bc7f08c3e63ed156cf78aefb4a6550d97b87997977ee"
//        val publicKeyBytes = Hex.decode(publicKey)
//
//        val privateKey = "f0106660c3dda23f16daa9ac5b811b963077f5bc0af89f85804f0de8e424f050"
//        val privateKeyBytes = Hex.decode(privateKey)

        val bip = Bip39()
//        val entr = bip.generateEntropy("fog shadow perfect fiction gesture famous pet original aim topic pepper need")
//        val seed = bip.generateSeed(entr, "")

        val seed = Hex.decode("82eb5cf58d175aaf389a0dacb16ef78f712fbc1a4712e2d57fcf5fd5e8ec3f13")
        val keypair = KeypairFactory().generate(EncryptionType.ECDSA, seed, "")

        val signer = Signer()

        val result = signer.sign(EncryptionType.ECDSA, messageHex.toByteArray(), keypair)

        assert(signer.verifyECDSA(messageHex.toByteArray(), result, keypair.publicKey))
    }
}