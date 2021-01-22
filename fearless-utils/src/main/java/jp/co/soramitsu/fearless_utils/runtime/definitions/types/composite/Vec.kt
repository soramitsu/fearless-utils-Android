package jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.runtime.definitions.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.stub.replaceStubsWithChild
import jp.co.soramitsu.fearless_utils.scale.dataType.compactInt

class Vec(name: String, val type: Type<*>) : Type<List<*>>(name) {

    override fun replaceStubs(registry: TypeRegistry): Vec {
        return replaceStubsWithChild(registry, type) { newChild ->
            Vec(name, newChild)
        }
    }

    override fun decode(scaleCodecReader: ScaleCodecReader): List<*> {
        val size = compactInt.read(scaleCodecReader)
        val result = mutableListOf<Any?>()

        repeat(size.toInt()) {
            val element = type.decode(scaleCodecReader)
            result.add(element)
        }

        return result
    }

    override fun encode(scaleCodecWriter: ScaleCodecWriter, value: List<*>) {
        val size = value.size.toBigInteger()
        compactInt.write(scaleCodecWriter, size)

        value.forEach {
            type.encodeUnsafe(scaleCodecWriter, it)
        }
    }

    override fun isValidInstance(instance: Any?): Boolean {
        return instance is List<*> && instance.all { type.isValidInstance(instance) }
    }
}