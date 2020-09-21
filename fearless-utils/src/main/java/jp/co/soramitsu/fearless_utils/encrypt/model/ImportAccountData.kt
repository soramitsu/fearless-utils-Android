package jp.co.soramitsu.fearless_utils.encrypt.model

import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType
import jp.co.soramitsu.fearless_utils.ss58.AddressType

data class ImportAccountData(
    val keypair: Keypair,
    val encryptionType: EncryptionType,
    val networType: AddressType,
    val username: String,
    val address: String
)

class ImportAccountMeta(
    val name: String,
    val networkType: AddressType,
    val encryptionType: EncryptionType
)