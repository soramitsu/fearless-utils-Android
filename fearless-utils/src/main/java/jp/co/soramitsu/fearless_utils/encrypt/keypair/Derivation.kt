package jp.co.soramitsu.fearless_utils.encrypt.keypair

import jp.co.soramitsu.fearless_utils.junction.JunctionDecoder

internal fun <K : Keypair> KeypairFactory<K>.generate(
    junctionDecoder: JunctionDecoder,
    seed: ByteArray,
    derivationPath: String
): K {
    val parentKeypair = deriveFromSeed(seed)

    if (derivationPath.isEmpty()) return parentKeypair

    val decodeResult = junctionDecoder.decode(derivationPath)

    return decodeResult.junctions.fold(parentKeypair) { currentKeyPair, junction ->
        deriveChild(currentKeyPair, junction)
    }
}