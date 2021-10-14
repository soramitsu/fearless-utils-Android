package jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics

import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.DictEnum

class MultiSignature(val encryptionType: EncryptionType, val value: ByteArray)

fun Extrinsic.Signature.tryExtractMultiSignature(): MultiSignature? {
    val enumEntry = signature as? DictEnum.Entry<*> ?: return null
    val value = enumEntry.value as? ByteArray ?: return null

    val encryptionType =
        EncryptionType.fromStringOrNull(enumEntry.name.toLowerCase()) ?: return null

    return MultiSignature(encryptionType, value)
}

fun <A> Extrinsic.Signature.Companion.new(
    accountIdentifier: A,
    signature: Any?,
    signedExtras: ExtrinsicPayloadExtrasInstance
) = Extrinsic.Signature(
    accountIdentifier = accountIdentifier,
    signature = signature,
    signedExtras = signedExtras
)

fun multiAddressFromId(addressId: ByteArray): DictEnum.Entry<ByteArray> {
    return DictEnum.Entry(
        name = MULTI_ADDRESS_ID,
        value = addressId
    )
}