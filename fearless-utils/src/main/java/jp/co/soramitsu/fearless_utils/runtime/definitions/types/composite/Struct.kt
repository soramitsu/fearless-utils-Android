package jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.fearless_utils.runtime.definitions.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.stub.replaceStubsWithChildren

@Suppress("UNCHECKED_CAST")
class Struct(
    name: String,
    val mapping: LinkedHashMap<String, Type<*>>
) : Type<Struct.Instance>(name) {

    class Instance(val mapping: Map<String, Any?>) {
        inline operator fun <reified R> get(key: String): R? = mapping[key] as? R
    }

    override fun replaceStubs(registry: TypeRegistry): Struct {
        return replaceStubsWithChildren(registry, mapping) { newChildren ->
            Struct(name, newChildren)
        }
    }

    override fun decode(scaleCodecReader: ScaleCodecReader): Instance {
        val values = mapping.mapValues { (_, type) ->
            type.decode(scaleCodecReader)
        }

        return Instance(values)
    }

    override fun encode(scaleCodecWriter: ScaleCodecWriter, value: Instance) {
        mapping.forEach { (name, type) ->
            type.encodeUnsafe(scaleCodecWriter, value[name])
        }
    }

    override fun isValidInstance(instance: Any?): Boolean {
        if (instance !is Instance) return false

        return mapping.all { (key, child) ->
            child.isValidInstance(instance[key])
        }
    }

    inline operator fun <reified R> get(key: String): R? = mapping[key] as? R
}