package jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.runtime.definitions.TypeRegistry

class DictEnum(name: String, val elements: List<Entry<Type<*>>>) :
    Type<DictEnum.Entry<Any?>>(name) {

    class Entry<out T>(val name: String, val value: T)

    override fun replaceStubs(registry: TypeRegistry): DictEnum {
        var changed = 0

        val newChildren = elements.map { entry ->
            val newType = entry.value.replaceStubs(registry)

            if (newType !== entry.value) changed++

            Entry(entry.name, newType)
        }

        return if (changed > 0) DictEnum(name, newChildren) else this
    }

    override fun decode(scaleCodecReader: ScaleCodecReader): Entry<Any?> {
        val typeIndex = scaleCodecReader.readByte()
        val entry = elements[typeIndex.toInt()]

        val decoded = entry.value.decode(scaleCodecReader)

        return Entry(entry.name, decoded)
    }

    override fun encode(scaleCodecWriter: ScaleCodecWriter, value: Entry<Any?>) {
        val index = elements.indexOfFirst { it.name == value.name }

        if (index == -1) {
            throw  IllegalArgumentException("No ${value.name} in ${elements.map(Entry<*>::name)}")
        }

        val type = elements[index].value

        scaleCodecWriter.writeByte(index)
        type.encodeUnsafe(scaleCodecWriter, value.value)
    }

    override fun isValidInstance(instance: Any?): Boolean {
        if (instance !is Entry<*>) return false

        val elementEntry = elements.find { it.name == instance.name } ?: return false

        return elementEntry.value.isValidInstance(instance.value)
    }
}
