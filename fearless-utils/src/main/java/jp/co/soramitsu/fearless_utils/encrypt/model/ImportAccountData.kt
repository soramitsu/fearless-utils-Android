package jp.co.soramitsu.fearless_utils.encrypt.model

import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType
import jp.co.soramitsu.fearless_utils.ss58.AddressType

class ImportAccountData(
    val keypair: Keypair,
    val encryptionType: EncryptionType,
    val username: String?,
    val networkInformation: NetworkSensitiveInformation?,
    val seed: ByteArray? = null
) {
    class NetworkSensitiveInformation(
        val addressType: AddressType,
        val address: String
    )
}

class ImportAccountMeta(
    val name: String?,
    val networkType: AddressType?,
    val encryptionType: EncryptionType
)