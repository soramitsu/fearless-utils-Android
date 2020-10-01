package jp.co.soramitsu.fearless_utils.wsrpc

import com.google.gson.Gson
import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketAdapter
import com.neovisionaries.ws.client.WebSocketException
import com.neovisionaries.ws.client.WebSocketFactory
import jp.co.soramitsu.fearless_utils.wsrpc.request.base.RpcRequest
import jp.co.soramitsu.fearless_utils.wsrpc.response.RpcResponse

interface Logger {
    fun log(message: String?)

    fun log(throwable: Throwable?)
}

class WebSocketWrapper(
    url: String,
    listener: WebSocketResponseListener,
    val singleResponse: Boolean = true,
    val logger: Logger? = null
) {

    private val ws = WebSocketFactory().createSocket(url)
    private val gson = Gson()

    init {
        ws.addListener(object : WebSocketAdapter() {
            override fun onTextMessage(websocket: WebSocket?, text: String?) {
                logger?.log("[RECEIVED] $text")

                listener.onResponse(gson.fromJson(text, RpcResponse::class.java))

                if (singleResponse) {
                    ws.disconnect()
                }
            }

            override fun onError(websocket: WebSocket?, cause: WebSocketException?) {
                logger?.log(cause)

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

        logger?.log("[SENDING] $text")

        ws.sendText(text)
    }
}