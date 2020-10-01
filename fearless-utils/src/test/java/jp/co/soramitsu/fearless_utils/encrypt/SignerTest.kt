package jp.co.soramitsu.fearless_utils.encrypt

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SignerTest {

    @Test
    fun `should sign message ED25519`() {
        val messageHex = "this is a message"

        val keypair = KeypairFactory().generate(EncryptionType.ED25519, TestData.SEED_BYTES, "")

        val signer = Signer()

        val result = signer.sign(EncryptionType.ED25519, messageHex.toByteArray(), keypair)

        assert(signer.verifyEd25519(messageHex.toByteArray(), result.signature!!, keypair.publicKey))
    }

    @Test
    fun `should sign message ECDSA`() {
        val messageHex = "this is a message"

        val keypair = KeypairFactory().generate(EncryptionType.ECDSA, TestData.SEED_BYTES, "")

        val signer = Signer()

        val result = signer.sign(EncryptionType.ECDSA, messageHex.toByteArray(), keypair)

        assert(signer.verifyECDSA(messageHex.toByteArray(), result, keypair.publicKey))
    }
}