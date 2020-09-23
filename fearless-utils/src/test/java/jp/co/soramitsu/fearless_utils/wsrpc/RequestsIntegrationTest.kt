package jp.co.soramitsu.fearless_utils.wsrpc

import io.reactivex.Single
import jp.co.soramitsu.fearless_utils.ss58.AddressType
import jp.co.soramitsu.fearless_utils.ss58.SS58Encoder
import jp.co.soramitsu.fearless_utils.wsrpc.request.base.RpcRequest
import jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.account.AccountInfoRequest
import jp.co.soramitsu.fearless_utils.wsrpc.response.RpcResponse
import org.bouncycastle.util.encoders.Hex
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class RequestsIntegrationTest {
    private val encoder = SS58Encoder()

    @Test
    fun `should fetch null balance`() {
        val url = "wss://kusama-rpc.polkadot.io"
        val publicKey = "6c88e9f8a5b39f1ac58f74569a62fb5c4738e7a8e42a6e312486c24af6686369"
        val publicKeyBytes = Hex.decode(publicKey)

        val single = startSocketAsSingle(url, AccountInfoRequest(publicKeyBytes))

        val response = single.blockingGet()

        assert(response.result == null)
    }

    @Test
    fun `should fetch existing balance`() {
        val url = "wss://westend-rpc.polkadot.io"
        val address = "5DEwU2U97RnBHCpfwHMDfJC7pqAdfWaPFib9wiZcr2ephSfT"

        val publicKey = encoder.decode(address, AddressType.WESTEND)

        val single = startSocketAsSingle(url, AccountInfoRequest(publicKey))

        val response = single.blockingGet()

        assert(response.result != null)
    }

    private fun startSocketAsSingle(url: String, request: RpcRequest) : Single<RpcResponse> {
        return Single.fromPublisher<RpcResponse> {
            val webSocket = WebSocketWrapper(url, object : WebSocketResponseListener {
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
}