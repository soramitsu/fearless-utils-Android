package jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.runtime.definitions.TypeRegistry
import jp.co.soramitsu.fearless_utils.scale.dataType.compactInt
import java.math.BigInteger

class Compact(name: String, val type: NumberType) : Type<BigInteger>(name) {

    // no stubs since inner type is number
    override fun replaceStubs(registry: TypeRegistry): Compact {
        return this
    }

    override fun decode(scaleCodecReader: ScaleCodecReader): BigInteger {
        return compactInt.read(scaleCodecReader)
    }

    override fun encode(scaleCodecWriter: ScaleCodecWriter, value: BigInteger) {
        return compactInt.write(scaleCodecWriter, value)
    }

    override fun isValidInstance(instance: Any?): Boolean {
        return type.isValidInstance(instance)
    }
}