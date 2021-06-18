package jp.co.soramitsu.schema.definitions.types.composite

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.schema.definitions.types.Type
import jp.co.soramitsu.schema.Context
import jp.co.soramitsu.schema.definitions.types.TypeReference

class Alias(alias: String, val aliasedReference: TypeReference) : Type<Any?>(alias) {

    override fun decode(scaleCodecReader: ScaleCodecReader, context: Context): Any? {
        return aliasedReference.requireValue().decode(scaleCodecReader, context)
    }

    override fun encode(scaleCodecWriter: ScaleCodecWriter, runtime: Context, value: Any?) {
        aliasedReference.requireValue().encodeUnsafe(scaleCodecWriter, runtime, value)
    }

    override fun isValidInstance(instance: Any?): Boolean {
        return aliasedReference.requireValue().isValidInstance(instance)
    }

    override val isFullyResolved: Boolean
        get() = aliasedReference.isResolved()
}
