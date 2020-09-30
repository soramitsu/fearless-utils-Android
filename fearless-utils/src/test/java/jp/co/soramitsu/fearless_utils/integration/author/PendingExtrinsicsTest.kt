package jp.co.soramitsu.fearless_utils.integration.author

import jp.co.soramitsu.fearless_utils.integration.executeRequest
import jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.author.PendingExtrinsicsRequest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class PendingExtrinsicsTest {

    @Test
    fun `should get pending extrinsics`() {
        val url = "wss://kusama-rpc.polkadot.io"
        val request = PendingExtrinsicsRequest()

        val result = executeRequest(url, request).blockingGet()

        print(result)

        assert(result.result is List<*>)
    }
}