package jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.schema.definitions.types.primitives.Primitive
import jp.co.soramitsu.schema.Context

object Null : Primitive<Any?>("Null") {

    override fun decode(scaleCodecReader: ScaleCodecReader, context: Context): Any? {
        return null
    }

    override fun encode(scaleCodecWriter: ScaleCodecWriter, runtime: Context, value: Any?) {
        // pass
    }

    override fun isValidInstance(instance: Any?): Boolean {
        return instance == null
    }
}
