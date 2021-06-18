package jp.co.soramitsu.schema.definitions.types.composite

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.schema.Context
import jp.co.soramitsu.schema.definitions.types.TypeReference
import jp.co.soramitsu.schema.definitions.types.primitives.NumberType
import java.math.BigInteger

class SetType(
    name: String,
    valueTypeReference: TypeReference,
    val valueList: LinkedHashMap<String, BigInteger>
) : WrapperType<Set<String>>(name, valueTypeReference) {

    override fun decode(scaleCodecReader: ScaleCodecReader, context: Context): Set<String> {
        val valueType = typeReference.requireValue()
        require(valueType is NumberType)

        val value = valueType.decode(scaleCodecReader, context)

        return valueList.mapNotNullTo(mutableSetOf()) { (name, mask) ->
            if (value.and(mask).signum() == 1) {
                name
            } else {
                null
            }
        }
    }

    override fun encode(scaleCodecWriter: ScaleCodecWriter, runtime: Context, value: Set<String>) {
        val valueType = typeReference.requireValue()
        require(valueType is NumberType)

        val folded = valueList.entries.fold(BigInteger.ZERO) { acc, (name, mask) ->
            if (name in value) {
                acc + mask
            } else {
                acc
            }
        }

        valueType.encode(scaleCodecWriter, runtime, folded)
    }

    override fun isValidInstance(instance: Any?): Boolean {
        return instance is Set<*> && instance.all { item -> item in valueList }
    }

    operator fun get(key: String) = valueList[key]
}
