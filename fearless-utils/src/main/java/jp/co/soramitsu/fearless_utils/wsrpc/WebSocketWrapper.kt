package jp.co.soramitsu.fearless_utils.wsrpc

import com.google.gson.Gson
import com.neovisionaries.ws.client.WebSocketFactory
import com.neovisionaries.ws.client.WebSocketAdapter
import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketException

class WebSocketWrapper(url: String, listener: WebSocketResponseListener) {

    private val ws = WebSocketFactory().createSocket(url)
    private val gson = Gson()

    init {
        ws.addListener(object : WebSocketAdapter() {
            override fun onTextMessage(websocket: WebSocket?, text: String?) {
                super.onTextMessage(websocket, text)
                listener.onResponse(gson.fromJson(text, RpcResponse::class.java))
            }

            override fun onError(websocket: WebSocket?, cause: WebSocketException?) {
                super.onError(websocket, cause)
                listener.onError(cause!!)
            }
        })
        ws.connectAsynchronously()
    }

    fun sendRpcMessage(rpcRequest: RpcRequest) {
        val text = gson.toJson(rpcRequest)
        ws.sendText(text)
    }
}