package jp.co.soramitsu.fearless_utils.integration.author

import jp.co.soramitsu.fearless_utils.integration.BaseIntegrationTest
import jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.author.PendingExtrinsicsRequest
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
@Ignore("Manual run only")
class PendingExtrinsicsTest : BaseIntegrationTest() {

    @Test
    fun `should get pending extrinsics`() {
        val request = PendingExtrinsicsRequest()

        val result = socketService.executeRequest(request).blockingGet()

        print(result)

        assert(result.result is List<*>)
    }
}