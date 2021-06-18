package jp.co.soramitsu.schema.definitions.types.composite

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.schema.definitions.types.TypeReference
import jp.co.soramitsu.schema.definitions.types.primitives.BooleanType
import jp.co.soramitsu.schema.RuntimeSnapshot
import jp.co.soramitsu.schema.definitions.types.errors.EncodeDecodeException

class Option(
    name: String,
    typeReference: TypeReference
) : WrapperType<Any?>(name, typeReference) {

    override fun decode(scaleCodecReader: ScaleCodecReader, runtime: RuntimeSnapshot): Any? {
        if (typeReference.requireValue() is BooleanType) {
            return when (scaleCodecReader.readByte().toInt()) {
                0 -> null
                1 -> false
                2 -> true
                else -> throw EncodeDecodeException("Not a optional boolean")
            }
        }

        val some: Boolean = scaleCodecReader.readBoolean()

        return if (some) typeReference.requireValue().decode(scaleCodecReader, runtime) else null
    }

    override fun encode(scaleCodecWriter: ScaleCodecWriter, runtime: RuntimeSnapshot, value: Any?) {
        val type = typeReference.requireValue()

        if (type is BooleanType) {
            scaleCodecWriter.writeOptional(ScaleCodecWriter.BOOL, value as Boolean)
        } else {
            if (value == null) {
                scaleCodecWriter.write(ScaleCodecWriter.BOOL, false)
            } else {
                scaleCodecWriter.write(ScaleCodecWriter.BOOL, true)
                type.encodeUnsafe(scaleCodecWriter, runtime, value)
            }
        }
    }

    override fun isValidInstance(instance: Any?): Boolean {
        return instance == null || typeReference.requireValue().isValidInstance(instance)
    }
}