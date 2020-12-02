package jp.co.soramitsu.fearless_utils.integration.chain

import jp.co.soramitsu.fearless_utils.integration.BaseIntegrationTest
import jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.chain.RuntimeVersionRequest
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
@Ignore("Manual run only")
class RuntimeVersionRequestTest : BaseIntegrationTest() {

    @Test
    fun `should fetch runtime version`() {
        val request = RuntimeVersionRequest()

        val result = socketService.executeRequest(request).blockingGet()

        print(result)

        assert(result.result is Map<*, *>)
    }
}