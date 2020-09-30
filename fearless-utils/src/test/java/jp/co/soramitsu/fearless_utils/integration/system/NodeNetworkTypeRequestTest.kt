package jp.co.soramitsu.fearless_utils.integration.system

import jp.co.soramitsu.fearless_utils.integration.executeRequest
import jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.system.NodeNetworkTypeRequest
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
@Ignore("Manual run only")
class NodeNetworkTypeRequestTest {

    @Test
    fun `should get node network type`() {
        val url = "wss://westend-rpc.polkadot.io"

        val response = executeRequest(url, NodeNetworkTypeRequest()).blockingGet()

        val type = response.result as String

        assert(type == "Westend")
    }
}