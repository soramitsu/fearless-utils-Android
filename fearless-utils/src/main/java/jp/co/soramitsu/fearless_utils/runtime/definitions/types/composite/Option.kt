package jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.runtime.definitions.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.BooleanType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.stub.replaceStubsWithChild

class Option(name: String, val type: Type<*>) : Type<Any?>(name) {

    override fun replaceStubs(registry: TypeRegistry): Option {
        return replaceStubsWithChild(registry, type) { newChildren ->
            Option(name, newChildren)
        }
    }

    override fun decode(scaleCodecReader: ScaleCodecReader): Any? {
        if (type is BooleanType) {
            return when (scaleCodecReader.readByte().toInt()) {
                0 -> null
                1 -> false
                2 -> true
                else -> throw IllegalArgumentException("Not a optional boolean")
            }
        }

        val some: Boolean = scaleCodecReader.readBoolean()

        return if (some) type.decode(scaleCodecReader) else null
    }

    override fun encode(scaleCodecWriter: ScaleCodecWriter, value: Any?) {
        if (type is BooleanType) {
            scaleCodecWriter.writeOptional(ScaleCodecWriter.BOOL, value as Boolean)
        } else {
            if (value == null) {
                scaleCodecWriter.write(ScaleCodecWriter.BOOL, false)
            } else {
                scaleCodecWriter.write(ScaleCodecWriter.BOOL, true)
                type.encodeUnsafe(scaleCodecWriter, value)
            }
        }
    }

    override fun isValidInstance(instance: Any?): Boolean {
        return instance == null || type.isValidInstance(instance)
    }
}