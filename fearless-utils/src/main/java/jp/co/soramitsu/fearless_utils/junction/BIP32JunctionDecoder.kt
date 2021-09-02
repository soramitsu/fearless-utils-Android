@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS")

package jp.co.soramitsu.fearless_utils.junction

import jp.co.soramitsu.fearless_utils.extensions.requireOrException
import jp.co.soramitsu.fearless_utils.extensions.toUnsignedBytes

private const val HARD_KEY_FLAG = 0x80000000u

@OptIn(ExperimentalUnsignedTypes::class)
object BIP32JunctionDecoder : JunctionDecoder() {

    sealed class DecodingError : Exception() {
        object InvalidBIP32Junction : Exception()
        object InvalidBIP32HardJunction : Exception()
    }

    override fun decodeJunction(rawJunction: String, type: JunctionType): Junction {
        val numericJunction = rawJunction.toUIntOrNull()
            ?: throw DecodingError.InvalidBIP32Junction

        requireOrException(numericJunction < HARD_KEY_FLAG) {
            DecodingError.InvalidBIP32HardJunction
        }

        val adjustedJunction = if (type == JunctionType.HARD) {
            numericJunction or HARD_KEY_FLAG
        } else {
            numericJunction
        }

        return Junction(type, chaincode = adjustedJunction.toUnsignedBytes())
    }
}