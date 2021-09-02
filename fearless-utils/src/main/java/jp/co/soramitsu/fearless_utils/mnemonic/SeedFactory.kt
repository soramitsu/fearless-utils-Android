package jp.co.soramitsu.fearless_utils.mnemonic

interface SeedFactory {

    class Result(val seed: ByteArray, val mnemonic: Mnemonic)

    fun createSeed(length: Mnemonic.Length, password: String?): Result

    fun deriveSeed(mnemonicWords: String, password: String?): Result
}