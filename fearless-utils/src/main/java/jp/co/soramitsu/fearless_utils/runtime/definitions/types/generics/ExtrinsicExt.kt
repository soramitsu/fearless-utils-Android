package jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics

import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.DictEnum

class MultiSignature(val encryptionType: EncryptionType, val value: ByteArray)

fun Extrinsic.ExtrinsicSignature.tryExtractMultiSignature(): MultiSignature? {
    val enumEntry = signature as? DictEnum.Entry<*> ?: return null
    val value = enumEntry.value as? ByteArray ?: return null

    val encryptionType =
        EncryptionType.fromStringOrNull(enumEntry.name.toLowerCase()) ?: return null

    return MultiSignature(encryptionType, value)
}

fun MultiSignature.asExtrinsicSignature(): Any {
    val entryName = encryptionType.rawName.capitalize()

    return DictEnum.Entry(entryName, value)
}

fun <A> Extrinsic.ExtrinsicSignature.Companion.new(
    accountIdentifier: A,
    signature: MultiSignature,
    signedExtras: SignedExtrasInstance
) = Extrinsic.ExtrinsicSignature(
    accountIdentifier = accountIdentifier,
    signature = signature.asExtrinsicSignature(),
    signedExtras = signedExtras
)

fun Extrinsic.ExtrinsicSignature.Companion.newV27(
    accountId: ByteArray,
    signature: MultiSignature,
    signedExtras: SignedExtrasInstance
) = Extrinsic.ExtrinsicSignature.new(
    accountIdentifier = accountId,
    signature = signature,
    signedExtras = signedExtras
)

fun Extrinsic.ExtrinsicSignature.Companion.newV28(
    accountId: ByteArray,
    signature: MultiSignature,
    signedExtras: SignedExtrasInstance
) = Extrinsic.ExtrinsicSignature.new(
    accountIdentifier = multiAddressFromId(accountId),
    signature = signature,
    signedExtras = signedExtras
)

fun multiAddressFromId(addressId: ByteArray): DictEnum.Entry<ByteArray> {
    return DictEnum.Entry(
        name = MULTI_ADDRESS_ID,
        value = addressId
    )
}