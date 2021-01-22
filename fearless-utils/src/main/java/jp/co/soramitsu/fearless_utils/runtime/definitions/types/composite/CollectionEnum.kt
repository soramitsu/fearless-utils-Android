package jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.fearless_utils.runtime.definitions.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.scale.dataType.CollectionEnumType

class CollectionEnum(
    name: String,
    private val elements: List<String>
) : Type<String>(name) {

    // no stubs possible
    override fun replaceStubs(registry: TypeRegistry): Type<String> = this

    override fun decode(scaleCodecReader: ScaleCodecReader): String {
        return CollectionEnumType(elements).read(scaleCodecReader)
    }

    override fun encode(scaleCodecWriter: ScaleCodecWriter, value: String) {
        CollectionEnumType(elements).write(scaleCodecWriter, value)
    }

    override fun isValidInstance(instance: Any?): Boolean {
        return instance in elements
    }
}