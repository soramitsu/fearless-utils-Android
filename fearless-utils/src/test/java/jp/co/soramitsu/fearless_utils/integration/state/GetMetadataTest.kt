package jp.co.soramitsu.fearless_utils.integration.state

import jp.co.soramitsu.fearless_utils.integration.BaseIntegrationTest
import jp.co.soramitsu.fearless_utils.scale.dataType.list
import jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.RuntimeRequest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

class GetMetadataRequest() : RuntimeRequest(
    method = "state_getMetadata",
    params = listOf()
)

@RunWith(MockitoJUnitRunner::class)
//@Ignore("Manual run only")
class GetMetadataTest : BaseIntegrationTest() {

    @Test
    fun `should fetch metadata`() {
        val request = GetMetadataRequest()

        val response = socketService.executeRequest(request).blockingGet()

        print(response.result)
    }
}