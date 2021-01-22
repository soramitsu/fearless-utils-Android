package jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.fearless_utils.runtime.definitions.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.stub.replaceStubsWithChildren

@Suppress("UNCHECKED_CAST")
class Struct(name: String, val children: LinkedHashMap<String, Type<*>>) :
    Type<Struct.Instance>(name) {

    class Instance(val values: Map<String, Any?>) {

        inline operator fun <reified R> get(key: String): R? = values[key] as? R
    }

    override fun replaceStubs(registry: TypeRegistry): Type<Instance> {
        return replaceStubsWithChildren(registry, children) { newChildren ->
            Struct(name, newChildren)
        }
    }

    override fun decode(scaleCodecReader: ScaleCodecReader): Instance {
        val values = children.mapValues { (_, type) ->
            type.decode(scaleCodecReader)
        }

        return Instance(values)
    }

    override fun encode(scaleCodecWriter: ScaleCodecWriter, value: Instance) {
        children.forEach { (name, type) ->
            type.encodeUnsafe(scaleCodecWriter, value[name])
        }
    }

    override fun isValidInstance(instance: Any?): Boolean {
        if (instance !is Map<*, *>) return false

        return children.all { (key, child) ->
            child.isValidInstance(instance[key])
        }
    }
}