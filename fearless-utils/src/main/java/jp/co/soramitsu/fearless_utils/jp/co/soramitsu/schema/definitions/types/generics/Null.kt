package jp.co.soramitsu.fearless_utils.jp.co.soramitsu.schema.definitions.types.generics

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.schema.definitions.types.primitives.Primitive
import jp.co.soramitsu.schema.RuntimeSnapshot

object Null : Primitive<Any?>("Null") {

    override fun decode(scaleCodecReader: ScaleCodecReader, runtime: RuntimeSnapshot): Any? {
        return null
    }

    override fun encode(scaleCodecWriter: ScaleCodecWriter, runtime: RuntimeSnapshot, value: Any?) {
        // pass
    }

    override fun isValidInstance(instance: Any?): Boolean {
        return instance == null
    }
}
