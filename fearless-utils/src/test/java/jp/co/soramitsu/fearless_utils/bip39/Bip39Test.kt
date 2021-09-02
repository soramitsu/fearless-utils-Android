package jp.co.soramitsu.fearless_utils.bip39

import com.google.gson.Gson
import jp.co.soramitsu.fearless_utils.common.getResourceReader
import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType
import jp.co.soramitsu.fearless_utils.encrypt.keypair.substrate.SubstrateKeypairFactory
import jp.co.soramitsu.fearless_utils.extensions.fromHex
import jp.co.soramitsu.fearless_utils.extensions.toHexString
import jp.co.soramitsu.fearless_utils.junction.SubstrateJunctionDecoder
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.spongycastle.util.encoders.Hex

@RunWith(MockitoJUnitRunner::class)
class Bip39Test {

    private lateinit var bip39: Bip39

    private val expectedEntropyHex = "2a5ecdeb7466f14d3c06d5aa5c6d433d"
    private val expectedMnemonic =
        "clean wait kiss trip humor pledge useless survey prevent toddler express knock"

    private val gson = Gson()

    @Before
    fun setUp() {
        bip39 = Bip39()
    }

    @Test
    fun generateMnemonic() {
        val mnemonic = bip39.generateMnemonic(MnemonicLength.TWELVE)
        val wordsCount = mnemonic.split(" ").size
        assertEquals(12, wordsCount)

        val mnemonic2 = bip39.generateMnemonic(MnemonicLength.TWENTY_ONE)
        val wordsCount2 = mnemonic2.split(" ").size

        assertEquals(21, wordsCount2)
    }

    @Test
    fun `should  pass ed25519 tests`() {
        performSpecTests("crypto/ed25519HDKD.json", EncryptionType.ED25519)
    }

    @Test
    fun `should  pass ecdsa tests`() {
        performSpecTests("crypto/ecdsaHDKD.json", EncryptionType.ECDSA)
    }

    private fun performSpecTests(filename: String, encryptionType: EncryptionType) {
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

            assertEquals(
                "Mnemonic=${testCase.mnemonic}, derivationPath=${testCase.path}",
                testCase.expectedPublicKey,
                actualKeypair.publicKey.toHexString(withPrefix = true)
            )
        }
    }

    @Test
    fun generateEntropy() {
        runMnemonicDecodingTest(expectedMnemonic)
    }

    @Test
    fun generateMnemonicFromEntropy() {
        val mnemonic = bip39.generateMnemonic(expectedEntropyHex.fromHex())

        assertEquals(expectedMnemonic, mnemonic)
    }

    @Test
    fun `should generate mnemonic from input with extra special symbols`() {
        runMnemonicDecodingTest(" clean wait kiss trip humor pledge useless survey prevent toddler express knock")
        runMnemonicDecodingTest("\t clean wait kiss trip humor pledge useless survey prevent toddler express knock")
        runMnemonicDecodingTest("\n clean wait kiss trip humor pledge useless survey prevent toddler express knock")
        runMnemonicDecodingTest("clean, wait\t kiss    trip,    humor\n pledge useless survey prevent toddler express knock")
    }

    @Test
    fun generateSeed() {
        val expectedSeed = "44e9d125f037ac1d51f0a7d3649689d422c2af8b1ec8e00d71db4d7bf6d127e3"
        val mnemonic =
            "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about"
        val passphrase = "Substrate"

        val seed = bip39.generateSeed(bip39.generateEntropy(mnemonic), passphrase)

        assertEquals(expectedSeed, Hex.toHexString(seed))
    }

    private fun runMnemonicDecodingTest(input: String) {
        val entropy = bip39.generateEntropy(input)

        assertEquals(expectedEntropyHex, entropy.toHexString())
    }
}