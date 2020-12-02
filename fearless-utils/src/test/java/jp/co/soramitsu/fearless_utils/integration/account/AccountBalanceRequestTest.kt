package jp.co.soramitsu.fearless_utils.integration.account

import com.google.gson.Gson
import jp.co.soramitsu.fearless_utils.integration.BaseIntegrationTest
import jp.co.soramitsu.fearless_utils.integration.KUSAMA_URL
import jp.co.soramitsu.fearless_utils.integration.WESTEND_URL
import jp.co.soramitsu.fearless_utils.ss58.AddressType
import jp.co.soramitsu.fearless_utils.ss58.SS58Encoder
import jp.co.soramitsu.fearless_utils.wsrpc.SocketService
import jp.co.soramitsu.fearless_utils.wsrpc.StdoutLogger
import jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.account.AccountInfoRequest
import jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.system.NodeNetworkTypeRequest
import jp.co.soramitsu.fearless_utils.wsrpc.response.RpcResponse
import org.bouncycastle.util.encoders.Hex
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
@Ignore("Manual run only")
class AccountBalanceRequestTest : BaseIntegrationTest(WESTEND_URL) {
    private val encoder = SS58Encoder()

    @Test
    fun `should fetch null balance`() {
        val publicKey = "6c88e9f8a5b39f1ac58f74569a62fb5c4738e7a8e42a6e312486c24af6686369"
        val publicKeyBytes = Hex.decode(publicKey)

        val single = socketService.executeRequest(AccountInfoRequest(publicKeyBytes))

        val response = single.blockingGet()

        assert(response.result == null)
    }

    @Test
    fun `should fetch existing balance`() {
        val address = "5DEwU2U97RnBHCpfwHMDfJC7pqAdfWaPFib9wiZcr2ephSfT"

        val publicKey = encoder.decode(address, AddressType.WESTEND)

        val single = socketService.executeRequest(AccountInfoRequest(publicKey))

        val response = single.blockingGet()

        assert(response.result != null)
    }
}