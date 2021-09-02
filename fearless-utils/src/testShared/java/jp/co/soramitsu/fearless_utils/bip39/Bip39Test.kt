package jp.co.soramitsu.fearless_utils.bip39

import com.google.gson.Gson
import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType
import jp.co.soramitsu.fearless_utils.encrypt.keypair.substrate.SubstrateKeypairFactory
import jp.co.soramitsu.fearless_utils.extensions.toHexString
import jp.co.soramitsu.fearless_utils.getResourceReader
import jp.co.soramitsu.fearless_utils.junction.SubstrateJunctionDecoder
import org.junit.Assert

abstract class Bip39Test {

    val bip39 = Bip39()
    val gson = Gson()

    protected fun performSpecTests(
        filename: String,
        encryptionType: EncryptionType
    ) {
        val testCases = gson.fromJson(
            getResourceReader(filename),
            Array<MnemonicTestCase>::class.java
        )

        testCases.forEach { testCase ->
            val derivationPathRaw = testCase.path.ifEmpty { null }

            val derivationPath = derivationPathRaw
                ?.let { SubstrateJunctionDecoder.decode(testCase.path) }

            val actualEntropy = bip39.generateEntropy(testCase.mnemonic)
            val actualSeed = bip39.generateSeed(actualEntropy, derivationPath?.password)

            val actualKeypair = SubstrateKeypairFactory.generate(
                seed = actualSeed,
                junctions = derivationPath?.junctions.orEmpty(),
                encryptionType = encryptionType
            )

            Assert.assertEquals(
                "Mnemonic=${testCase.mnemonic}, derivationPath=${testCase.path}",
                testCase.expectedPublicKey,
                actualKeypair.publicKey.toHexString(withPrefix = true)
            )
        }
    }
}

