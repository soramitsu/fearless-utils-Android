package jp.co.soramitsu.fearless_utils.encrypt.mnemonic

import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType
import org.junit.Test

class AndroidSubstrateSeedFactoryTest : SubstrateSeedFactoryTest() {

    /*
     Sr25519 cannot run on local machine since gradle rust plugin does not work with desktop targets
     To overcome, run tests on android device
     */
    @Test
    fun shouldRunSr25519Tests() {
        performSpecTests("crypto/sr25519HDKD.json", EncryptionType.SR25519)
    }
}