package jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.account

import jp.co.soramitsu.fearless_utils.runtime.Module
import jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.RuntimeRequest

private const val METHOD = "state_getStorage"

class AccountInfoRequest(publicKey: ByteArray) : RuntimeRequest(
    METHOD,
    listOf(
        Module.System.Account.storageKey(publicKey)
    )
)
