package jp.co.soramitsu.fearless_utils.encrypt.keypair.ethereum

import jp.co.soramitsu.fearless_utils.encrypt.keypair.Keypair
import jp.co.soramitsu.fearless_utils.encrypt.keypair.generate
import jp.co.soramitsu.fearless_utils.junction.Junction

object EthereumKeypairFactory {

    fun generate(seed: ByteArray, junctions: List<Junction>): Keypair {
        return Bip32KeypairFactory.generate(seed, junctions)
    }
}