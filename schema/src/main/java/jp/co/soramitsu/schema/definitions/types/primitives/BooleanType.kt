package jp.co.soramitsu.schema.definitions.types.primitives

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.schema.Context

object BooleanType : Primitive<Boolean>("bool") {

    override fun decode(scaleCodecReader: ScaleCodecReader, context: Context): Boolean {
        return scaleCodecReader.readBoolean()
    }

    override fun encode(scaleCodecWriter: ScaleCodecWriter, runtime: Context, value: Boolean) {
        scaleCodecWriter.write(ScaleCodecWriter.BOOL, value)
    }

    override fun isValidInstance(instance: Any?) = instance is Boolean
}
