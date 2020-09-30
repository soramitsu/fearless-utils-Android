package jp.co.soramitsu.fearless_utils.integration

import io.reactivex.Single
import jp.co.soramitsu.fearless_utils.wsrpc.WebSocketResponseListener
import jp.co.soramitsu.fearless_utils.wsrpc.WebSocketWrapper
import jp.co.soramitsu.fearless_utils.wsrpc.request.base.RpcRequest
import jp.co.soramitsu.fearless_utils.wsrpc.response.RpcResponse

fun executeRequest(url: String, request: RpcRequest) : Single<RpcResponse> {
    return Single.fromPublisher<RpcResponse> {
        val webSocket =
            WebSocketWrapper(
                url,
                object :
                    WebSocketResponseListener {
                    override fun onResponse(response: RpcResponse) {
                        it.onNext(response)
                        it.onComplete()
                    }

                    override fun onError(error: Throwable) {
                        it.onError(error)
                    }
                })

        webSocket.connect()

        webSocket.sendRpcRequest(request)
    }
}