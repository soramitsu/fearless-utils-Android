package jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.runtime.definitions.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.stub.replaceStubsWithChildren

class Tuple(name: String, val types: List<Type<*>>) : Type<List<*>>(name) {

    override fun replaceStubs(registry: TypeRegistry): Type<List<*>> {
        return replaceStubsWithChildren(registry, types) { newChildren ->
            Tuple(name, newChildren)
        }
    }

    override fun decode(scaleCodecReader: ScaleCodecReader): List<*> {
        return types.map { it.decode(scaleCodecReader) }
    }

    override fun encode(scaleCodecWriter: ScaleCodecWriter, value: List<*>) {
        types.zip(value).onEach { (type, value) ->
            type.encodeUnsafe(scaleCodecWriter, value)
        }
    }

    override fun isValidInstance(instance: Any?): Boolean {
        return instance is List<*> && types.zip(instance).all { (type, possibleValue) ->
            type.isValidInstance(possibleValue)
        }
    }
}