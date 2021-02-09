@file:Suppress("EXPERIMENTAL_API_USAGE")  // unsigned types

package jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.DictEnum
import jp.co.soramitsu.fearless_utils.scale.dataType.byte
import jp.co.soramitsu.fearless_utils.scale.dataType.compactInt

private val SIGNED_MASK = 0b1000_0000.toUByte()

class Extrinsic : Type<Extrinsic.Instance>("Extrinsic") {

    class Instance(
        val signature: ExtrinsicSignature?,
        val call: GenericCall.Instance
    )

    class ExtrinsicSignature(
        val accountIdentifier: Any?,
        val signature: DictEnum.Entry<Any?>,
        val signedExtras: Map<String, Any?>
    )

    override val isFullyResolved: Boolean = true

    override fun decode(scaleCodecReader: ScaleCodecReader, runtime: RuntimeSnapshot): Instance {
        val length = compactInt.read(scaleCodecReader)

        val extrinsicVersion = byte.read(scaleCodecReader).toUByte()

        val isSigned = isSigned(extrinsicVersion)

        return Instance(null, GenericCall.Instance(0, 0, mapOf()))
    }

    override fun encode(
        scaleCodecWriter: ScaleCodecWriter,
        runtime: RuntimeSnapshot,
        value: Instance
    ) {
        TODO("Not yet implemented")
    }

    override fun isValidInstance(instance: Any?): Boolean {
        return instance is Instance
    }

    private fun encodedVersion(version: UByte, isSigned: Boolean) : UByte {
        return if (isSigned) {
            version or SIGNED_MASK
        } else {
            version
        }
    }

    private fun isSigned(extrinsicVersion: UByte): Boolean {
        return extrinsicVersion and SIGNED_MASK != 0.toUByte()
    }
}