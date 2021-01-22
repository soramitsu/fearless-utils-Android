package jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.runtime.definitions.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.stub.replaceStubsWithChild

class FixedArray(name: String, val length: Int, val type: Type<*>) : Type<List<*>>(name) {

    override fun replaceStubs(registry: TypeRegistry): FixedArray {
        return replaceStubsWithChild(registry, type) { newChild ->
            FixedArray(name, length, newChild)
        }
    }

    override fun decode(scaleCodecReader: ScaleCodecReader): List<*> {
        val list = mutableListOf<Any?>()

        repeat(length) {
            list += type.decode(scaleCodecReader)
        }

        return list
    }

    override fun encode(scaleCodecWriter: ScaleCodecWriter, value: List<*>) {
        value.forEach {
            type.encodeUnsafe(scaleCodecWriter, it)
        }
    }

    override fun isValidInstance(instance: Any?): Boolean {
        return instance is List<*> && instance.all { type.isValidInstance(instance) }
    }
}