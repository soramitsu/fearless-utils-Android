package jp.co.soramitsu.fearless_utils.jp.co.soramitsu.schema.definitions.types.generics

import jp.co.soramitsu.schema.definitions.types.primitives.FixedByteArray

val H160 = Hash(160)
val H256 = Hash(256)
val H512 = Hash(512)

class Hash(bits: Int) : FixedByteArray("H$bits", length = bits / 8) {
    init {
        require(bits % 8 == 0)
    }
}
