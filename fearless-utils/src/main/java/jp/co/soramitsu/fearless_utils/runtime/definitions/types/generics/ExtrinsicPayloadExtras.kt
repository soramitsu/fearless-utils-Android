package jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.schema.definitions.types.Type
import jp.co.soramitsu.schema.definitions.types.errors.EncodeDecodeException
import jp.co.soramitsu.schema.definitions.types.primitives.Compact
import jp.co.soramitsu.schema.definitions.types.primitives.u32

typealias ExtrinsicPayloadExtrasInstance = Map<String, Any?>

private const val _MORTALITY = "CheckMortality"
private const val _NONCE = "CheckNonce"
private const val _TIP = "ChargeTransactionPayment"

class SignedExtras(runtime: RuntimeSnapshot) : ExtrinsicPayloadExtras(
    name = "SignedExtras",
    extrasMapping = mapOf(
        _MORTALITY to EraType,
        _NONCE to Compact("Compact<Index>"),
        _TIP to Compact("Compact<u32>")
    ),
    runtime
) {

    companion object {
        const val ERA = _MORTALITY
        const val NONCE = _NONCE
        const val TIP = _TIP
    }
}

private const val _GENESIS = "CheckGenesis"
private const val _SPEC_VERSION = "CheckSpecVersion"
private const val _TX_VERSION = "CheckTxVersion"

class AdditionalExtras(runtime: RuntimeSnapshot) : ExtrinsicPayloadExtras(
    name = "AdditionalExtras",
    extrasMapping = mapOf(
        _MORTALITY to H256,
        _GENESIS to H256,
        _SPEC_VERSION to u32,
        _TX_VERSION to u32
    ),
    runtime
) {

    companion object {
        const val BLOCK_HASH = _MORTALITY
        const val GENESIS = _GENESIS
        const val SPEC_VERSION = _SPEC_VERSION
        const val TX_VERSION = _TX_VERSION
    }
}

open class ExtrinsicPayloadExtras(
    name: String,
    private val extrasMapping: Map<String, Type<*>>,
    private val runtime: RuntimeSnapshot
) : Type<ExtrinsicPayloadExtrasInstance>(name) {

    override fun decode(
        scaleCodecReader: ScaleCodecReader,
    ): ExtrinsicPayloadExtrasInstance {
        val enabledSignedExtras = runtime.metadata.extrinsic.signedExtensions

        return enabledSignedExtras.associateWith { name ->
            extrasMapping[name]?.decode(scaleCodecReader)
        }
    }

    override fun encode(
        scaleCodecWriter: ScaleCodecWriter,
        value: ExtrinsicPayloadExtrasInstance
    ) {
        val enabledSignedExtras = runtime.metadata.extrinsic.signedExtensions

        return enabledSignedExtras.forEach { name ->
            extrasMapping[name]?.encodeUnsafe(scaleCodecWriter, value[name])
        }
    }

    override val isFullyResolved: Boolean = true

    override fun isValidInstance(instance: Any?): Boolean {
        return instance is Map<*, *> && instance.keys.all { it is String }
    }

    private fun unknownSignedExtension(name: String): Nothing =
        throw EncodeDecodeException("Unknown signed extension: $name")
}
