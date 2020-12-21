package jp.co.soramitsu.fearless_utils.wsrpc

import com.google.gson.Gson
import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketException
import com.neovisionaries.ws.client.WebSocketFactory
import com.neovisionaries.ws.client.WebSocketListener
import com.neovisionaries.ws.client.WebSocketState
import io.reactivex.subscribers.TestSubscriber
import jp.co.soramitsu.fearless_utils.common.assertThrows
import jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.RuntimeRequest
import jp.co.soramitsu.fearless_utils.wsrpc.response.RpcResponse
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.any
import org.mockito.Mockito.anyString
import org.mockito.junit.MockitoJUnitRunner

open class TestRequest(id: Int) : RuntimeRequest("test", emptyList(), id)

object SingeRequest : TestRequest(0)

fun testResponse(id: Int) = RpcResponse(jsonrpc = "2.0", result = "Test", id = id, error = null)

@RunWith(MockitoJUnitRunner::class)
class SocketServiceTest {
    @Mock
    private lateinit var socketFactory: WebSocketFactory

    @Mock
    private lateinit var ws: WebSocket

    private lateinit var socketService: SocketService

    private val gson = Gson()

    private var listener: WebSocketListener? = null

    private var successfulConnect = true

    @Before
    fun setup() {
        `when`(socketFactory.createSocket(anyString())).thenReturn(ws)

        `when`(ws.addListener(any())).then {
            listener = it.arguments[0] as WebSocketListener

            ws
        }

        `when`(ws.sendText(anyString())).then {
            val text = it.arguments[0] as String
            val request = gson.fromJson(text, RuntimeRequest::class.java)
            val response = testResponse(request.id)

            listener!!.onTextMessage(ws, gson.toJson(response))

            ws
        }

        `when`(ws.connectAsynchronously()).then {
            if (successfulConnect) {
                listener!!.onConnected(ws, mapOf())
                listener!!.onStateChanged(ws, WebSocketState.OPEN)
            } else {
                listener!!.onConnectError(ws, WebSocketException(null))
                listener!!.onStateChanged(ws, WebSocketState.CLOSED)
            }

            ws
        }

        socketService = SocketService(gson, StdoutLogger, socketFactory)
    }

    @After
    fun tearDown() {
        socketService.stop()
    }

    @Test
    fun `should send single request`() {
        successfulConnect = true

        socketService.start("test")

        val response = socketService.executeRequest(SingeRequest).blockingGet()

        assertEquals(testResponse(response.id).result, response.result)
    }

    @Test
    fun `should throw for at most once request error`() {
        successfulConnect = false

        socketService.start("test")

        assertThrows<ConnectionClosedException> {
            socketService.executeRequest(SingeRequest, deliveryType = DeliveryType.AT_MOST_ONCE)
                .blockingGet()
        }
    }

    @Test
    fun `should finish request after reconnecting`() {
        successfulConnect = false

        socketService.start("test")

        successfulConnect = true

        socketService.executeRequest(SingeRequest).blockingGet()
    }
}