package jp.co.soramitsu.fearless_utils.integration.extrinsic

import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType
import jp.co.soramitsu.fearless_utils.encrypt.model.Keypair
import jp.co.soramitsu.fearless_utils.integration.BaseIntegrationTest
import jp.co.soramitsu.fearless_utils.integration.WESTEND_URL
import jp.co.soramitsu.fearless_utils.runtime.RealRuntimeProvider
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.multiAddressFromId
import jp.co.soramitsu.fearless_utils.runtime.extrinsic.ExtrinsicBuilder
import jp.co.soramitsu.fearless_utils.runtime.extrinsic.transfer
import jp.co.soramitsu.fearless_utils.wsrpc.executeAsync
import jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.author.SubmitExtrinsicRequest
import jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.chain.RuntimeVersion
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import java.math.BigInteger

private val KEYPAIR = Keypair(
    publicKey = jp.co.soramitsu.schema.extensions.fromHex(),
    privateKey = jp.co.soramitsu.schema.extensions.fromHex()
)

@Ignore("Manual run only")
class SendIntegrationTest : BaseIntegrationTest(WESTEND_URL) {

    val runtime = RealRuntimeProvider.buildRuntime("westend")

    @Test
    fun `should form batch extrinsic so node accepts it`() = runBlocking {
        val builder = ExtrinsicBuilder(
            runtime = runtime,
            keypair = KEYPAIR,
            nonce = 38.toBigInteger(),
            runtimeVersion = RuntimeVersion(48, 4),
            genesisHash = jp.co.soramitsu.schema.extensions.fromHex(),
            encryptionType = EncryptionType.ED25519,
            accountIdentifier = multiAddressFromId(KEYPAIR.publicKey),
        )

        repeat(2) {
            builder.transfer(
                recipientAccountId = jp.co.soramitsu.schema.extensions.fromHex(),
                amount = BigInteger("5000000001")
            )
        }

        val extrinsic = builder.build()

        print(socketService.executeAsync(SubmitExtrinsicRequest(extrinsic)).result!!)
    }
}