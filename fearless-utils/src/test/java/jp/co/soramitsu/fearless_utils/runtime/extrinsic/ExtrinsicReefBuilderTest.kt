package jp.co.soramitsu.fearless_utils.runtime.extrinsic

import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType
import jp.co.soramitsu.fearless_utils.encrypt.MultiChainEncryption
import jp.co.soramitsu.fearless_utils.encrypt.keypair.BaseKeypair
import jp.co.soramitsu.fearless_utils.extensions.fromHex
import jp.co.soramitsu.fearless_utils.runtime.RealRuntimeProvider
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.DictEnum
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.fromHex
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.Era
import jp.co.soramitsu.fearless_utils.runtime.metadata.module
import jp.co.soramitsu.fearless_utils.runtime.metadata.storage
import jp.co.soramitsu.fearless_utils.ss58.SS58Encoder.toAccountId
import jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.chain.RuntimeVersion
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test
import java.math.BigInteger

class ExtrinsicReefBuilderTest {

    private val reefRuntime = RealRuntimeProvider.buildRuntime("reef")

    private val reefKeypair = BaseKeypair(
        privateKey = "fb981a1fe2aeb2c6f3eaf50af5ac2408b6f5ab4b04ae124aa4546d71014d6e0d".fromHex(),
        publicKey = "bcc5ecf679ebd776866a04c212a4ec5dc45cefab57d7aa858c389844e212693f".fromHex()
    )

    @Test
    fun `reef parse storage to get events for transfer extrinsic`(){
        val storage = "0x1400000000000000585f8f09000000000200000001000000000370bb1f4a1e4b3a5d75a514663a9f7037199646ce6778f70a644a6d742b841d5a000001000000060070bb1f4a1e4b3a5d75a514663a9f7037199646ce6778f70a644a6d742b841d5a00006a71c4a617727a000000000000000000010000000602fa60375f6f51700e52fe74d31d0f658dd73540828aae24c23bb9f703e1bf7a2a70bb1f4a1e4b3a5d75a514663a9f7037199646ce6778f70a644a6d742b841d5a00006a71c4a617727a000000000000000000010000000000c8c7ba0a00000000000000"
        val eventType =
            reefRuntime.metadata.module("System").storage("Events").type.value!!
        val eventsRaw = eventType.fromHex(reefRuntime, storage)
        assertEquals(5, (eventsRaw as List<*>).size)
    }

    @Test
    fun `should build single reef transfer extrinsic`() {
        val from = "5HizQmLThiXqRbheYrWJKjzk4wf7pe3s3LBHBdsCZ2CUm7RK"
        val to = "5EcWomY3YY1GCJh51tkww4TrEXAPiVLeYJPDXVeNSoDcVMJA"
        val extrinsicInHex =
            "0x450284fa60375f6f51700e52fe74d31d0f658dd73540828aae24c23bb9f703e1bf7a2a00769bef62c011346389cb613c0b0aa659538bc7b65b9c15a41429fcb39fee1bb25f06e8c387e0ed511d65017f1730168de34f71d007340ea3a044b4190611110d9502d400060370bb1f4a1e4b3a5d75a514663a9f7037199646ce6778f70a644a6d742b841d5a13000064a7b3b6e00d"

        val builder = ExtrinsicBuilder(
            runtime = reefRuntime,
            keypair = reefKeypair,
            nonce = 53.toBigInteger(),
            runtimeVersion = RuntimeVersion(32, 32),
            genesisHash = "7834781d38e4798d548e34ec947d19deea29df148a7bf32484b7b24dacf8d4b7".fromHex(),
            multiChainEncryption = MultiChainEncryption.Substrate(EncryptionType.ED25519),
            accountIdentifier = from.toAccountId(),
            era = Era.getEraFromBlockPeriod(12905, 64),
            blockHash = "0x45af4531f27b22ec5e46cd30ae7c7dc14ef1b2af7952389d3e4f0dfe7658fb91".fromHex()
        )

        builder.call(
            "Balances",
            "transfer_keep_alive",
            mapOf(
                "dest" to to.toAccountId(),
                "value" to BigInteger("1000000000000000000")
            )
        )

        val encoded = builder.build()

        assertEquals(extrinsicInHex, encoded)
    }
}