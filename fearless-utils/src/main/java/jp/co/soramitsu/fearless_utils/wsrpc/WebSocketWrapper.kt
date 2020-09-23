package jp.co.soramitsu.fearless_utils.wsrpc

import com.google.gson.Gson
import com.neovisionaries.ws.client.WebSocketFactory
import com.neovisionaries.ws.client.WebSocketAdapter
import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketException
import jp.co.soramitsu.fearless_utils.wsrpc.request.base.RpcRequest
import jp.co.soramitsu.fearless_utils.wsrpc.response.RpcResponse

class WebSocketWrapper(
    url: String,
    listener: WebSocketResponseListener,
    val singleResponse: Boolean = true
) {

    private val ws = WebSocketFactory().createSocket(url)
    private val gson = Gson()

    init {
        ws.addListener(object : WebSocketAdapter() {
            override fun onTextMessage(websocket: WebSocket?, text: String?) {
                super.onTextMessage(websocket, text)

                listener.onResponse(gson.fromJson(text, RpcResponse::class.java))

                if (singleResponse) {
                    ws.disconnect()
                }
            }

            override fun onError(websocket: WebSocket?, cause: WebSocketException?) {
                super.onError(websocket, cause)
                listener.onError(cause!!)
            }
        })
    }

    fun connect() {
        ws.connect()
    }

    fun connectAsync() {
        ws.connectAsynchronously()
    }

    fun disconnect() {
        ws.disconnect()
    }

    fun sendRpcRequest(rpcRequest: RpcRequest) {
        val text = gson.toJson(rpcRequest)
        ws.sendText(text)
    }
}