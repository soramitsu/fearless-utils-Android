package jp.co.soramitsu.fearless_utils.encrypt

import jp.co.soramitsu.fearless_utils.bip39.Bip39
import org.junit.Assert.*
import org.junit.Test

import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.spongycastle.util.encoders.Hex

@RunWith(MockitoJUnitRunner::class)
class KeypairFactoryTest {

    private lateinit var keypairFactory: KeypairFactory
    private lateinit var bip39: Bip39

    @Before
    fun setUp() {
        keypairFactory = KeypairFactory()
        bip39 = Bip39()
    }

    @Test
    fun decodeDerivationPath_called() {
        val address = "5DfhGyQdFobKM8NsWvEeAKk5EQQgYe9AydgJ7rMB6E1EqRzV"
        val expectedKey = "0x46ebddef8cd9bb167dc30878d7113b7e168e6f0646beffd77d69d39bad76b47a"
        val seed = Hex.decode("fac7959dbfe72f052e5a0c3c8d6530f202b02fd8f9f5ca3580ec8deb7797479e0b9f67282f42d1214e457243b9eb38a0a5ed13de66c01cfb213a6fe73cedef5a")
        val derivationPath = ""
//
        assertEquals(expectedKey, Hex.toHexString(keypairFactory.generate(EncryptionType.SR25519, seed, derivationPath).privateKey))
    }
}