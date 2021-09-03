package jp.co.soramitsu.fearless_utils.mnemonic.ethereum

import jp.co.soramitsu.fearless_utils.mnemonic.Mnemonic
import jp.co.soramitsu.fearless_utils.mnemonic.MnemonicCreator
import jp.co.soramitsu.fearless_utils.mnemonic.SeedCreator
import jp.co.soramitsu.fearless_utils.mnemonic.SeedFactory

object EthereumSeedFactory : SeedFactory {

    override fun createSeed(length: Mnemonic.Length, password: String?): SeedFactory.Result {
        val mnemonic = MnemonicCreator.randomMnemonic(length)
        val seed = SeedCreator.deriveSeed(mnemonic.words.encodeToByteArray(), password)

        return SeedFactory.Result(seed, mnemonic)
    }

    override fun deriveSeed(mnemonicWords: String, password: String?): SeedFactory.Result {
        val mnemonic = MnemonicCreator.fromWords(mnemonicWords)
        val seed = SeedCreator.deriveSeed(mnemonic.words.encodeToByteArray(), password)

        return SeedFactory.Result(seed, mnemonic)
    }
}
