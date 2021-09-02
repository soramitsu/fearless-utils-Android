package jp.co.soramitsu.fearless_utils.bip39

import androidx.test.ext.junit.runners.AndroidJUnit4
import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Bip39AndroidTest : Bip39Test() {

    /*
     Sr25519 cannot run on local machine since gradle rust plugin does not work with desktop targets
     To overcome, run tests on android device
     */
    @Test
    fun shouldRunSr25519Tests() {
        performSpecTests("crypto/sr25519HDKD.json", EncryptionType.SR25519)
    }
}