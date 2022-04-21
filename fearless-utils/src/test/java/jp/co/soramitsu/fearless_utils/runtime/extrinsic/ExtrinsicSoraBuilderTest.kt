package jp.co.soramitsu.fearless_utils.runtime.extrinsic

import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType
import jp.co.soramitsu.fearless_utils.encrypt.MultiChainEncryption
import jp.co.soramitsu.fearless_utils.encrypt.keypair.BaseKeypair
import jp.co.soramitsu.fearless_utils.extensions.fromHex
import jp.co.soramitsu.fearless_utils.runtime.RealRuntimeProvider
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.DictEnum
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.Era
import jp.co.soramitsu.fearless_utils.ss58.SS58Encoder.toAccountId
import jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.chain.RuntimeVersion
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigInteger

class ExtrinsicSoraBuilderTest {

    private val soraRuntime = RealRuntimeProvider.buildRuntime("sora2")

    private val soraKeypair = BaseKeypair(
        privateKey = "fb981a1fe2aeb2c6f3eaf50af5ac2408b6f5ab4b04ae124aa4546d71014d6e0d".fromHex(),
        publicKey = "bcc5ecf679ebd776866a04c212a4ec5dc45cefab57d7aa858c389844e212693f".fromHex()
    )

    @Test
    fun `should build single sora transfer extrinsic`() {
        val from = "cnVkoGs3rEMqLqY27c2nfVXJRGdzNJk2ns78DcqtppaSRe8qm"
        val to = "cnUVLAjzRsrXrzEiqjxMpBwvb6YgdBy8DKibonvZgtcQY5ZKe"
        val asset = "0200000000000000000000000000000000000000000000000000000000000000"
        val extrinsicInHex =
            "0xe1028483ba494b62a40d20c370e5381230d74b4e8906d0334a91777baef57c9a935467007a3855dd10d316c70dad4e4b88a857e2994017fac758f153d8f2cda5aba8cfbbbdd1cc3e73aa8b639bc6d45e151b61baa1f797370928b00d6fb7069ee6b1620f25000400140102000000000000000000000000000000000000000000000000000000000000007081dd99c361e7ccd05171ae67f7adcf2da5ea102ee65670db0d1190c7429674000014bbf08ac6020000000000000000"

        val builder = ExtrinsicBuilder(
            runtime = soraRuntime,
            keypair = soraKeypair,
            nonce = 1.toBigInteger(),
            runtimeVersion = RuntimeVersion(1, 1),
            genesisHash = "0f751ca2d30efe3385a4001d0bfa1548471babf5095f6fe88ee4813cf724fafc".fromHex(),
            multiChainEncryption = MultiChainEncryption.Substrate(EncryptionType.ED25519),
            accountIdentifier = from.toAccountId(),
            era = Era.getEraFromBlockPeriod(44866, 64),
            blockHash = "0xa532ea14451c9b4e1a9ed1c75ab67d8be659362c9d8f2206009ae8d62faf9fca".fromHex()
        )

        builder.call(
            "Assets",
            "transfer",
            mapOf(
                "asset_id" to asset.fromHex(),
                "to" to to.toAccountId(),
                "amount" to BigInteger("200000000000000000")
            )
        )

        val encoded = builder.build()

        assertEquals(extrinsicInHex, encoded)
    }

    @Test
    fun `sora swap extrinsic`() {
        val from = "cnVkoGs3rEMqLqY27c2nfVXJRGdzNJk2ns78DcqtppaSRe8qm"
        val asset = "0200000000000000000000000000000000000000000000000000000000000000"
        val asset2 = "0200040000000000000000000000000000000000000000000000000000000000"
        val extrinsicInHex =
            "0xe1028483ba494b62a40d20c370e5381230d74b4e8906d0334a91777baef57c9a935467007a3855dd10d316c70dad4e4b88a857e2994017fac758f153d8f2cda5aba8cfbbbdd1cc3e73aa8b639bc6d45e151b61baa1f797370928b00d6fb7069ee6b1620f25000400140102000000000000000000000000000000000000000000000000000000000000007081dd99c361e7ccd05171ae67f7adcf2da5ea102ee65670db0d1190c7429674000014bbf08ac6020000000000000000"

        val builder = ExtrinsicBuilder(
            runtime = soraRuntime,
            keypair = soraKeypair,
            nonce = 1.toBigInteger(),
            runtimeVersion = RuntimeVersion(1, 1),
            genesisHash = "0f751ca2d30efe3385a4001d0bfa1548471babf5095f6fe88ee4813cf724fafc".fromHex(),
            multiChainEncryption = MultiChainEncryption.Substrate(EncryptionType.ED25519),
            accountIdentifier = from.toAccountId(),
            era = Era.getEraFromBlockPeriod(44866, 64),
            blockHash = "0xa532ea14451c9b4e1a9ed1c75ab67d8be659362c9d8f2206009ae8d62faf9fca".fromHex()
        )

        builder.call(
            "LiquidityProxy",
            "swap",
            mapOf(
                "dex_id" to BigInteger("0"),
                "input_asset_id" to asset.fromHex(),
                "output_asset_id" to asset2.fromHex(),
                "swap_amount" to DictEnum.Entry("WithDesiredInput", Struct.Instance(mapOf("desired_amount_in" to BigInteger("1"), "min_amount_out" to BigInteger("2")))),
                "selected_source_types" to listOf("XYKPool"),
                "filter_mode" to "Disabled"
            )
        )

        val encoded = builder.build()

        assertEquals(extrinsicInHex, encoded)
    }
}