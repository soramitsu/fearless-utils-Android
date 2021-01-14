package jp.co.soramitsu.fearless_utils.integration.chain

import jp.co.soramitsu.fearless_utils.integration.BaseIntegrationTest
import jp.co.soramitsu.fearless_utils.wsrpc.executeAsync
import jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.chain.RuntimeVersionRequest
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
@Ignore("Manual run only")
class RuntimeVersionRequestTest : BaseIntegrationTest() {

    @Test
    fun `should fetch runtime version`() = runBlocking {
        val request = RuntimeVersionRequest()

        val result = socketService.executeAsync(request)

        print(result)

        assert(result.result is Map<*, *>)
    }
}
