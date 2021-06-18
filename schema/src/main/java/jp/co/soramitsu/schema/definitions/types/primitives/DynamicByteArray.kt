package jp.co.soramitsu.schema.definitions.types.primitives

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.schema.scale.dataType.byteArray
import jp.co.soramitsu.schema.Context

class DynamicByteArray(name: String) : Primitive<ByteArray>(name) {

    override fun decode(scaleCodecReader: ScaleCodecReader, context: Context): ByteArray {
        return byteArray.read(scaleCodecReader)
    }

    override fun encode(scaleCodecWriter: ScaleCodecWriter, runtime: Context, value: ByteArray) {
        return byteArray.write(scaleCodecWriter, value)
    }

    override fun isValidInstance(instance: Any?): Boolean {
        return instance is ByteArray
    }
}
