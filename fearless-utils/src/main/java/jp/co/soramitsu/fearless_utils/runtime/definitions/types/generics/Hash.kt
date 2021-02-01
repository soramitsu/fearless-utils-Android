package jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics

import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.FixedByteArray

val h160 = Hash(160)
val h256 = Hash(256)
val h512 = Hash(512)

class Hash(bits: Int) : FixedByteArray("H${bits}", length = bits / 8) {
    init {
        require(bits % 8 == 0)
    }
}