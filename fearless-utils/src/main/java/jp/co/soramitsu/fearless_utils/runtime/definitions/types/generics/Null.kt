package jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.fearless_utils.runtime.definitions.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type

object Null : Type<Any?>("Null") {

    override fun replaceStubs(registry: TypeRegistry):  Null = this

    override fun decode(scaleCodecReader: ScaleCodecReader): Any? {
        return null
    }

    override fun encode(scaleCodecWriter: ScaleCodecWriter, value: Any?) {
        // pass
    }

    override fun isValidInstance(instance: Any?): Boolean {
        return instance == null
    }
}