package jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.TypeReference
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.DictEnum
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.errors.EncodeDecodeException
import jp.co.soramitsu.fearless_utils.scale.dataType.byte

class ResultType(ok: TypeReference, err: TypeReference) : DictEnum(
    "Result", listOf(
        Entry(Ok, ok),
        Entry(Err, err),
    )
) {

    companion object {
        const val Ok = "Ok"
        const val Err = "Err"
    }

    override fun decode(scaleCodecReader: ScaleCodecReader, runtime: RuntimeSnapshot): Entry<Any?> {

        return when (val typeIndex = byte.read(scaleCodecReader).toInt()) {
            0 -> {
                val instance = elements[0].value.requireValue().decode(scaleCodecReader, runtime)
                Entry(Ok, instance)
            }

            1 -> {
                val instance = elements[1].value.requireValue().decode(scaleCodecReader, runtime)
                Entry(Err, instance)
            }

            else -> throw EncodeDecodeException("Cannot process $typeIndex for Result type")
        }
    }

    override fun encode(
        scaleCodecWriter: ScaleCodecWriter,
        runtime: RuntimeSnapshot,
        value: Entry<Any?>
    ) {
        when (value.name) {
            Ok -> {
                val index = 0
                byte.write(scaleCodecWriter, index.toByte())
                elements[0].value.requireValue()
                    .encodeUnsafe(scaleCodecWriter, runtime, value.value)
            }
            else -> {
                val index = 1
                byte.write(scaleCodecWriter, index.toByte())
                elements[1].value.requireValue()
                    .encodeUnsafe(scaleCodecWriter, runtime, value.value)
            }
        }
    }
}
