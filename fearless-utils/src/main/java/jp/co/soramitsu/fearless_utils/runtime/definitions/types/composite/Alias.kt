package jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.TypeReference

class Alias(alias: String, val aliasedReference: TypeReference) : Type<Any?>(alias) {

    override fun decode(scaleCodecReader: ScaleCodecReader): Any? {
        return aliasedReference.requireValue().decode(scaleCodecReader)
    }

    override fun encode(scaleCodecWriter: ScaleCodecWriter, value: Any?) {
        aliasedReference.requireValue().encodeUnsafe(scaleCodecWriter, value)
    }

    override fun isValidInstance(instance: Any?): Boolean {
        return aliasedReference.requireValue().isValidInstance(instance)
    }

    override val isFullyResolved: Boolean
        get() = aliasedReference.isResolved()
}