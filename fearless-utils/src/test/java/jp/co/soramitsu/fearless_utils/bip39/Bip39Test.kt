package jp.co.soramitsu.fearless_utils.bip39

import io.github.novacrypto.bip39.Words
import org.junit.Test

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.spongycastle.util.encoders.Hex

@RunWith(MockitoJUnitRunner::class)
class Bip39Test {

    private lateinit var bip39: Bip39

    @Before
    fun setUp() {
        bip39 = Bip39()
    }

    @Test
    fun generateMnemonic() {
        val mnemonic = bip39.generateMnemonic(Words.TWELVE)
        val wordsCount = mnemonic.split(" ").size
        assertEquals(12, wordsCount)

        val mnemonic2 = bip39.generateMnemonic(Words.TWENTY_ONE)
        val wordsCount2 = mnemonic2.split(" ").size

        assertEquals(21, wordsCount2)
    }

    @Test
    fun generateEntropy() {
        val expectedEntropyHex = "2a5ecdeb7466f14d3c06d5aa5c6d433d"
        val mnemonic = "clean wait kiss trip humor pledge useless survey prevent toddler express knock"

        val entropy = bip39.generateEntropy(mnemonic)

        assertEquals(expectedEntropyHex, Hex.toHexString(entropy))
    }

    @Test
    fun generateMnemonicFromEntropy() {
        val entropyHex = "2a5ecdeb7466f14d3c06d5aa5c6d433d"
        val expectedMnemonic = "clean wait kiss trip humor pledge useless survey prevent toddler express knock"

        val mnemonic = bip39.generateMnemonic(Hex.decode(entropyHex))

        assertEquals(expectedMnemonic, mnemonic)
    }

    @Test
    fun generateSeed() {
        val expectedSeed = "44e9d125f037ac1d51f0a7d3649689d422c2af8b1ec8e00d71db4d7bf6d127e33f50c3d5c84fa3e5399c72d6cbbbbc4a49bf76f76d952f479d74655a2ef2d453"
        val mnemonic = "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about"
        val passphrase = "Substrate"

        val seed = bip39.generateSeed(bip39.generateEntropy(mnemonic), passphrase)

        assertEquals(expectedSeed, Hex.toHexString(seed))
    }
}