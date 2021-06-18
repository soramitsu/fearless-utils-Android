package jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.schema.Context
import jp.co.soramitsu.schema.definitions.types.primitives.Primitive
import jp.co.soramitsu.schema.scale.dataType.byteArraySized
import jp.co.soramitsu.schema.extensions.fromHex


object CallBytes : Primitive<String>("CallBytes") {

    override fun decode(scaleCodecReader: ScaleCodecReader, context: Context): String {
        throw NotImplementedError() // the same as in polkascan implementation
    }

    override fun encode(scaleCodecWriter: ScaleCodecWriter, runtime: Context, value: String) {
        val bytes = value.fromHex()

        byteArraySized(bytes.size).write(scaleCodecWriter, bytes)
    }

    override fun isValidInstance(instance: Any?): Boolean {
        return instance is String
    }
}
