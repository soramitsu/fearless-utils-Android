package jp.co.soramitsu.fearless_utils.integration.chain

import jp.co.soramitsu.fearless_utils.integration.executeRequest
import jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.chain.RuntimeVersionRequest
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
@Ignore("Manual run only")
class RuntimeVersionRequestTest {

    @Test
    fun `should fetch runtime version`() {
        val url = "wss://kusama-rpc.polkadot.io"
        val request = RuntimeVersionRequest()

        val result = executeRequest(url, request).blockingGet()

        print(result)

        assert(result.result is Map<*, *>)
    }
}