@file:Suppress("EXPERIMENTAL_API_USAGE") // unsigned types

package jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot

import jp.co.soramitsu.schema.definitions.types.Type
import jp.co.soramitsu.schema.definitions.types.bytes
import jp.co.soramitsu.schema.definitions.types.errors.EncodeDecodeException
import jp.co.soramitsu.schema.definitions.types.toByteArray
import jp.co.soramitsu.schema.scale.dataType.byte
import jp.co.soramitsu.schema.scale.dataType.compactInt

private val SIGNED_MASK = 0b1000_0000.toUByte()

private const val TYPE_ADDRESS = "Address"
private const val TYPE_SIGNATURE = "ExtrinsicSignature"

class Extrinsic(private val context: RuntimeSnapshot) : Type<Extrinsic.Instance>("ExtrinsicsDecoder") {

    class Instance(
        val signature: Signature?,
        val call: GenericCall.Instance
    )

    class Signature(
        val accountIdentifier: Any?,
        val signature: Any?,
        val signedExtras: ExtrinsicPayloadExtrasInstance
    ) {
        companion object // for creator extensions
    }

    override val isFullyResolved: Boolean = true

    override fun decode(scaleCodecReader: ScaleCodecReader): Instance {
        val length = compactInt.read(scaleCodecReader)

        val extrinsicVersion = byte.read(scaleCodecReader).toUByte()

        val signature = if (isSigned(extrinsicVersion)) {
            Signature(
                accountIdentifier = addressType(context).decode(scaleCodecReader),
                signature = signatureType(context).decode(scaleCodecReader),
                signedExtras = SignedExtras(context).decode(scaleCodecReader, )
            )
        } else {
            null
        }

        val call = GenericCall(context).decode(scaleCodecReader)

        return Instance(signature, call)
    }

    override fun encode(
        scaleCodecWriter: ScaleCodecWriter,
        value: Instance
    ) {
        val isSigned = value.signature != null

        val extrinsicVersion = context.metadata.extrinsic.version.toInt().toUByte()
        val encodedVersion = encodedVersion(extrinsicVersion, isSigned).toByte()

        val signatureWrapperBytes = if (isSigned) {
            val signature = value.signature!!

            val addressBytes = addressType(context).bytes(signature.accountIdentifier)
            val signatureBytes = signatureType(context).bytes(signature.signature)
            val signedExtrasBytes = SignedExtras(context).bytes(signature.signedExtras)

            addressBytes + signatureBytes + signedExtrasBytes
        } else {
            byteArrayOf()
        }

        val callBytes = GenericCall(context).toByteArray(value.call)

        val extrinsicBodyBytes = byteArrayOf(encodedVersion) + signatureWrapperBytes + callBytes

        Bytes.encode(scaleCodecWriter, extrinsicBodyBytes)
    }

    override fun isValidInstance(instance: Any?): Boolean {
        return instance is Instance
    }

    private fun encodedVersion(version: UByte, isSigned: Boolean): UByte {
        return if (isSigned) {
            version or SIGNED_MASK
        } else {
            version
        }
    }

    private fun isSigned(extrinsicVersion: UByte): Boolean {
        return extrinsicVersion and SIGNED_MASK != 0.toUByte()
    }

    private fun addressType(context: RuntimeSnapshot): Type<*> {
        return context.typeRegistry[TYPE_ADDRESS]
            ?: requiredTypeNotFound(TYPE_ADDRESS)
    }

    private fun signatureType(context: RuntimeSnapshot): Type<*> {
        return context.typeRegistry[TYPE_SIGNATURE]
            ?: requiredTypeNotFound(TYPE_SIGNATURE)
    }

    private fun requiredTypeNotFound(name: String): Nothing {
        throw EncodeDecodeException("Cannot resolve $name type, which is required to work with Extrinsic")
    }
}
