package jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypePresetBuilder
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.getOrCreate
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.TypeReference
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.DictEnum
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.errors.EncodeDecodeException
import jp.co.soramitsu.fearless_utils.scale.dataType.byte
import jp.co.soramitsu.fearless_utils.scale.utils.directWrite

private const val NONE = "None"
private const val RAW = "Raw"
private const val BLAKE_2B_256 = "BlakeTwo256"
private const val SHA_256 = "Sha256"
private const val KECCAK_256 = "Keccak256"
private const val SHA_3_256 = "ShaThree256"

class Data(preset: TypePresetBuilder) : DictEnum("Data", createMapping(preset)) {

    override fun decode(scaleCodecReader: ScaleCodecReader, runtime: RuntimeSnapshot): Entry<Any?> {

        return when (val typeIndex = byte.read(scaleCodecReader).toInt()) {
            0 -> Entry(NONE, null)

            in 1..33 -> {
                val offset = 1
                val data = scaleCodecReader.readByteArray(typeIndex - offset)

                Entry(RAW, data)
            }

            in 34..37 -> {
                val offset = 32

                val typeEntry = elements[typeIndex - offset]

                val decoded = typeEntry.value.requireValue().decode(scaleCodecReader, runtime)

                Entry(typeEntry.name, decoded)
            }

            else -> throw EncodeDecodeException("Cannot process $typeIndex for Data type")
        }
    }

    override fun encode(
        scaleCodecWriter: ScaleCodecWriter,
        runtime: RuntimeSnapshot,
        value: Entry<Any?>
    ) {
        when (value.name) {
            NONE -> byte.write(scaleCodecWriter, 0)
            RAW -> {
                val raw = value.value
                require(raw is ByteArray && raw.size in 0..32)
                val index = raw.size + 1

                byte.write(scaleCodecWriter, index.toByte())
                scaleCodecWriter.directWrite(raw)
            }
            else -> {
                val offset = 32
                val index = elements.indexOfFirst { it.name == value.name }

                if (index == -1) elementNotFound(value.name)

                val type = elements[index].value.requireValue()

                scaleCodecWriter.writeByte(index + offset)
                type.encodeUnsafe(scaleCodecWriter, runtime, value.value)
            }
        }
    }
}

private fun createMapping(preset: TypePresetBuilder): List<DictEnum.Entry<TypeReference>> {
    return listOf(
        DictEnum.Entry(NONE, preset.getOrCreate("Null")),
        DictEnum.Entry(RAW, preset.getOrCreate("Bytes")),
        DictEnum.Entry(BLAKE_2B_256, preset.getOrCreate("H256")),
        DictEnum.Entry(SHA_256, preset.getOrCreate("H256")),
        DictEnum.Entry(KECCAK_256, preset.getOrCreate("H256")),
        DictEnum.Entry(SHA_3_256, preset.getOrCreate("H256"))
    )
}