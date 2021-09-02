package jp.co.soramitsu.fearless_utils.mnemonic.substrate

import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType
import jp.co.soramitsu.fearless_utils.mnemonic.SubstrateSeedFactoryTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class LocalSubstrateSeedFactoryTest : SubstrateSeedFactoryTest() {

    @Test
    fun `should  pass ed25519 tests`() {
        performSpecTests("crypto/ed25519HDKD.json", EncryptionType.ED25519)
    }

    @Test
    fun `should pass ecdsa tests`() {
        performSpecTests("crypto/ecdsaHDKD.json", EncryptionType.ECDSA)
    }
}