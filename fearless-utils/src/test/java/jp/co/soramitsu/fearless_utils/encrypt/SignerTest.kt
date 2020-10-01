package jp.co.soramitsu.fearless_utils.encrypt

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.spongycastle.util.encoders.Hex

@RunWith(MockitoJUnitRunner::class)
class SignerTest {

    @Test
    fun `should sign message`() {
        val messageHex = "this is a message"

        val seed = Hex.decode("82eb5cf58d175aaf389a0dacb16ef78f712fbc1a4712e2d57fcf5fd5e8ec3f13")
        val keypair = KeypairFactory().generate(EncryptionType.ECDSA, seed, "")

        val signer = Signer()

        val result = signer.sign(EncryptionType.ECDSA, messageHex.toByteArray(), keypair)

        assert(signer.verifyECDSA(messageHex.toByteArray(), result, keypair.publicKey))
    }
}