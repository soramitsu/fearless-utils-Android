package jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.schema.definitions.types.Type
import jp.co.soramitsu.schema.definitions.types.fromByteArray
import jp.co.soramitsu.schema.definitions.types.toByteArray

class OpaqueCall(val runtime: RuntimeSnapshot) : Type<GenericCall.Instance>("OpaqueCall") {

    override val isFullyResolved = true

    override fun decode(
        scaleCodecReader: ScaleCodecReader
    ): GenericCall.Instance {
        val bytes = Bytes.decode(scaleCodecReader)

        return GenericCall(runtime).fromByteArray(bytes)
    }

    override fun encode(
        scaleCodecWriter: ScaleCodecWriter,
        value: GenericCall.Instance
    ) {
        val callEncoded = GenericCall(runtime).toByteArray(value)

        return Bytes.encode(scaleCodecWriter, callEncoded)
    }

    override fun isValidInstance(instance: Any?): Boolean {
        return instance is ByteArray
    }
}
