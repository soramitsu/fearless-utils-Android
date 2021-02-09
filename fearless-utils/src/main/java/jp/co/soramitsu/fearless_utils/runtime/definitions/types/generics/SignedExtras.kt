package jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.errors.EncodeDecodeException
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.Compact

typealias SignedExtrasInstance = Map<String, Any?>

@Suppress("MemberVisibilityCanBePrivate")
object SignedExtras : Type<SignedExtrasInstance>("SignedExtras") {

    const val ERA = "CheckMortality"
    const val NONCE = "CheckNonce"
    const val TIP = "ChargeTransactionPayment"

    private val EXTENSION_MAPPING = mapOf(
        "CheckSpecVersion" to Null,
        "CheckTxVersion" to Null,
        "CheckGenesis" to Null,
        ERA to EraType,
        NONCE to Compact("Compact<Index>"),
        "CheckWeight" to Null,
        TIP to Compact("Compact<u32>") // tip
    )

    override fun decode(
        scaleCodecReader: ScaleCodecReader,
        runtime: RuntimeSnapshot
    ): SignedExtrasInstance {
        val enabledSignedExtras = runtime.metadata.extrinsic.signedExtensions

        return enabledSignedExtras.associateWith { name ->
            val type = getExtensionType(name)

            type.decode(scaleCodecReader, runtime)
        }
    }

    override fun encode(
        scaleCodecWriter: ScaleCodecWriter,
        runtime: RuntimeSnapshot,
        value: SignedExtrasInstance
    ) {
        val enabledSignedExtras = runtime.metadata.extrinsic.signedExtensions

        return enabledSignedExtras.forEach { name ->
            val type = getExtensionType(name)

            type.encodeUnsafe(scaleCodecWriter, runtime, value[name])
        }
    }

    override val isFullyResolved: Boolean = true

    override fun isValidInstance(instance: Any?): Boolean {
        return instance is Map<*, *> && instance.keys.all { it is String }
    }

    private fun getExtensionType(name: String) =
        EXTENSION_MAPPING[name] ?: unknownSignedExtension(name)

    private fun unknownSignedExtension(name: String): Nothing =
        throw EncodeDecodeException("Unknown signed extension: $name")
}