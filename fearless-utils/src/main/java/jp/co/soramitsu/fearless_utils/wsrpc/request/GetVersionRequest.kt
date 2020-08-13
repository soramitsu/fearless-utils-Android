package jp.co.soramitsu.fearless_utils.wsrpc.request

data class GetVersionRequest(
    val id: Int,
    val method: String
) : RpcRequest()