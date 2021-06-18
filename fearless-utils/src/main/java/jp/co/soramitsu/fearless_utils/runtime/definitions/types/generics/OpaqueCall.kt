package jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.schema.Context
import jp.co.soramitsu.schema.definitions.types.Type
import jp.co.soramitsu.schema.definitions.types.fromByteArray
import jp.co.soramitsu.schema.definitions.types.toByteArray

object OpaqueCall : Type<GenericCall.Instance>("OpaqueCall") {

    override val isFullyResolved = true

    override fun decode(
        scaleCodecReader: ScaleCodecReader,
        context: Context
    ): GenericCall.Instance {
        val bytes = Bytes.decode(scaleCodecReader, context)

        return GenericCall.fromByteArray(context, bytes)
    }

    override fun encode(
        scaleCodecWriter: ScaleCodecWriter,
        runtime: Context,
        value: GenericCall.Instance
    ) {
        val callEncoded = GenericCall.toByteArray(runtime, value)

        return Bytes.encode(scaleCodecWriter, runtime, callEncoded)
    }

    override fun isValidInstance(instance: Any?): Boolean {
        return instance is ByteArray
    }
}
