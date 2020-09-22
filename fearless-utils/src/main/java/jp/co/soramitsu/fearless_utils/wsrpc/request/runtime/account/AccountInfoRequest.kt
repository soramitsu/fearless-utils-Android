package jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.account

import jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.Module
import jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.RuntimeRequest
import jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.StorageUtils

private const val SERVICE_ACCOUNT = "Account"
private const val METHOD = "state_getStorage"

class AccountInfoRequest(publicKey: ByteArray) : RuntimeRequest(
    METHOD,
    listOf(
        StorageUtils.createStorageKey(Module.System.id, SERVICE_ACCOUNT, publicKey)
    )
)
