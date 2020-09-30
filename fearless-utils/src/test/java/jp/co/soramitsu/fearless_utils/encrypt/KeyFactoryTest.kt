package jp.co.soramitsu.fearless_utils.encrypt

import org.bouncycastle.util.encoders.Hex
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class KeyFactoryTest {

    private val keypairFactory = KeypairFactory()

    @Test
    fun `should generate keypair`() {
        val private = "f0106660c3dda23f16daa9ac5b811b963077f5bc0af89f85804f0de8e424f050"
        val public = "2f8c6129d816cf51c374bc7f08c3e63ed156cf78aefb4a6550d97b87997977ee"

        val seed = Hex.decode("3132333435363738393031323334353637383930313233343536373839303132")

        val keypair = keypairFactory.generate(EncryptionType.ED25519, seed, "")

        val actualPrivate = Hex.toHexString(keypair.privateKey)
        val actualPublic = Hex.toHexString(keypair.publicKey)

//        assertEquals(private, actualPrivate)
        assertEquals(public, actualPublic)
    }
}