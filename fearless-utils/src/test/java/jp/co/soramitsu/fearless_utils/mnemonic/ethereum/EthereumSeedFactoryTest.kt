package jp.co.soramitsu.fearless_utils.mnemonic.ethereum

import com.google.gson.Gson
import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType
import jp.co.soramitsu.fearless_utils.encrypt.keypair.ethereum.EthereumKeypairFactory
import jp.co.soramitsu.fearless_utils.extensions.toHexString
import jp.co.soramitsu.fearless_utils.getResourceReader
import jp.co.soramitsu.fearless_utils.junction.BIP32JunctionDecoder
import jp.co.soramitsu.fearless_utils.mnemonic.MnemonicTestCase
import org.junit.Assert
import org.junit.Test

class EthereumSeedFactoryTest {

    val gson = Gson()

    @Test
    fun `should run tests from json`() {
        val testCases = gson.fromJson(
            getResourceReader("crypto/BIP32HDKD.json"),
            Array<MnemonicTestCase>::class.java
        )

        testCases.forEach { testCase ->
            val derivationPathRaw = testCase.path.ifEmpty { null }

            val derivationPath = derivationPathRaw
                ?.let { BIP32JunctionDecoder.decode(testCase.path) }

            val result = EthereumSeedFactory.deriveSeed(testCase.mnemonic, derivationPath?.password)

            val actualKeypair = EthereumKeypairFactory.generate(
                seed = result.seed,
                junctions = derivationPath?.junctions.orEmpty()
            )

            Assert.assertEquals(
                "Mnemonic=${testCase.mnemonic}, derivationPath=${testCase.path}",
                testCase.expectedPublicKey,
                actualKeypair.publicKey.toHexString(withPrefix = true)
            )
        }
    }
}