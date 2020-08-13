package jp.co.soramitsu.fearless_utils.wsrpc

import jp.co.soramitsu.fearless_utils.wsrpc.response.RpcResponse

interface WebSocketResponseListener {

    fun onResponse(response: RpcResponse)
    fun onError(error: Throwable)
}