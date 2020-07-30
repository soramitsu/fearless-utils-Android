package jp.co.soramitsu.fearless_utils.wsrpc

interface WebSocketResponseListener {

    fun onResponse(response: RpcResponse)
    fun onError(error: Throwable)
}