package jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.fearless_utils.hash.isPositive
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.runtime.definitions.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.NumberType
import java.math.BigInteger

class SetType(
    name: String,
    val valueType: NumberType,
    val valueList: LinkedHashMap<String, BigInteger>
) : Type<Set<String>>(name) {

    // no stubs possible, since valueType is NumberType
    override fun replaceStubs(registry: TypeRegistry): SetType = this

    override fun decode(scaleCodecReader: ScaleCodecReader): Set<String> {
        val value = valueType.decode(scaleCodecReader)

        return valueList.mapNotNullTo(mutableSetOf()) { (name, mask) ->
            if (value.and(mask).isPositive()) {
                name
            } else {
                null
            }
        }
    }

    override fun encode(scaleCodecWriter: ScaleCodecWriter, value: Set<String>) {

        val folded = valueList.entries.fold(BigInteger.ZERO) { acc, (name, mask) ->
            if (name in value) {
                acc + mask
            } else {
                acc
            }
        }

        valueType.encodeNumber(scaleCodecWriter, folded)
    }

    override fun isValidInstance(instance: Any?): Boolean {
        return instance is Set<*> && instance.all { item -> item in valueList }
    }

    operator fun get(key: String) = valueList[key]
}