package jp.co.soramitsu.fearless_utils.encrypt.keypair.ethereum

import android.util.JsonReader
import com.google.gson.Gson
import jp.co.soramitsu.fearless_utils.common.getResourceReader
import jp.co.soramitsu.fearless_utils.encrypt.keypair.SeedTestCase
import jp.co.soramitsu.fearless_utils.extensions.fromHex
import jp.co.soramitsu.fearless_utils.extensions.toHexString
import org.junit.Assert.*
import org.junit.Test

class EthereumKeypairFactoryTest {

    private val gson = Gson()

    @Test
    fun `should pass spec tests`() {
        val testCases = gson.fromJson(
            getResourceReader("crypto/BIP32HDKDEtalon.json"),
            Array<SeedTestCase>::class.java
        )

        testCases.forEach(::performTest)
    }

    private fun performTest(testCase: SeedTestCase) {
        val actualKeypair = EthereumKeypairFactory.generate(
            seed = testCase.seed.fromHex(),
            derivationPath = testCase.path
        )

        assertEquals(
            "Seed=${testCase.seed}, derivationPath=${testCase.path}",
            testCase.expectedPublicKey,
            actualKeypair.publicKey.toHexString(withPrefix = true)
        )
    }
}