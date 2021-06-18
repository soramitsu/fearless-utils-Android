package jp.co.soramitsu.fearless_utils.runtime.extrinsic

import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType
import jp.co.soramitsu.fearless_utils.encrypt.model.Keypair
import jp.co.soramitsu.fearless_utils.runtime.RealRuntimeProvider
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.Era
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.multiAddressFromId
import jp.co.soramitsu.fearless_utils.ss58.SS58Encoder.toAccountId
import jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.chain.RuntimeVersion
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigInteger

private val KEYPAIR = Keypair(
    publicKey = jp.co.soramitsu.schema.extensions.fromHex(),
    privateKey = jp.co.soramitsu.schema.extensions.fromHex()
)

class ExtrinsicBuilderTest {

    val runtime = RealRuntimeProvider.buildRuntime("westend")

    @Test
    fun `should build single sora transfer extrinsic`() {
        val soraRuntime = RealRuntimeProvider.buildRuntime("sora2")
        val soraKeypair = Keypair(
            privateKey = jp.co.soramitsu.schema.extensions.fromHex(),
            publicKey = jp.co.soramitsu.schema.extensions.fromHex()
        )
        val from = "5F3RU8neUpkZJK7QxAHJ9TGDjUiyjfufpZvaXDBEifPkeJSz"
        val to = "5EcDoG4T1SLbop4bxBjLL9VJaaytZxGXA7mLaY9y84GYpzsR"
        val asset = "0200000000000000000000000000000000000000000000000000000000000000"
        val extrinsicInHex =
            "0xe1028483ba494b62a40d20c370e5381230d74b4e8906d0334a91777baef57c9a935467007a3855dd10d316c70dad4e4b88a857e2994017fac758f153d8f2cda5aba8cfbbbdd1cc3e73aa8b639bc6d45e151b61baa1f797370928b00d6fb7069ee6b1620f25000400140102000000000000000000000000000000000000000000000000000000000000007081dd99c361e7ccd05171ae67f7adcf2da5ea102ee65670db0d1190c7429674000014bbf08ac6020000000000000000"

        val builder = ExtrinsicBuilder(
            runtime = soraRuntime,
            keypair = soraKeypair,
            nonce = 1.toBigInteger(),
            runtimeVersion = RuntimeVersion(1, 1),
            genesisHash = jp.co.soramitsu.schema.extensions.fromHex(),
            encryptionType = EncryptionType.ED25519,
            accountIdentifier = from.toAccountId(),
            era = Era.getEraFromBlockPeriod(44866, 64),
            blockHash = jp.co.soramitsu.schema.extensions.fromHex()
        )

        builder.call(
            "Assets",
            "transfer",
            mapOf(
                "asset_id" to jp.co.soramitsu.schema.extensions.fromHex(),
                "to" to to.toAccountId(),
                "amount" to BigInteger("200000000000000000")
            )
        )

        val encoded = builder.build()

        assertEquals(extrinsicInHex, encoded)
    }

    @Test
    fun `should build single transfer extrinsic`() {
        val extrinsicInHex =
            "0x41028400fdc41550fb5186d71cae699c31731b3e1baa10680c7bd6b3831a6d222cf4d16800080bfe8bc67f44b498239887dc5679523cfcb1d20fd9ec9d6bae0a385cca118d2cb7ef9f4674d52a810feb32932d7c6fe3e05ce9e06cd72cf499c8692206410ab5038800040000340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c630700e40b5402"

        val builder = ExtrinsicBuilder(
            runtime = runtime,
            keypair = KEYPAIR,
            nonce = 34.toBigInteger(),
            runtimeVersion = RuntimeVersion(48, 4),
            genesisHash = jp.co.soramitsu.schema.extensions.fromHex(),
            encryptionType = EncryptionType.ED25519,
            accountIdentifier = multiAddressFromId(KEYPAIR.publicKey),
            era = Era.Mortal(64, 59),
            blockHash = jp.co.soramitsu.schema.extensions.fromHex()
        )

        builder.transfer(
            recipientAccountId = jp.co.soramitsu.schema.extensions.fromHex(),
            amount = BigInteger("10000000000")
        )

        val encoded = builder.build()

        assertEquals(extrinsicInHex, encoded)
    }

    @Test
    fun `should build batch extrinsic`() {

        val extrinsicInHex =
            "0xf1028400fdc41550fb5186d71cae699c31731b3e1baa10680c7bd6b3831a6d222cf4d168005b94d4436372ba74895936695e97d543358219e77f3e827f77b2e26f53413363a5dd098e172a51308e7d35aa6c03c5f171c4b43732db61c3d86b62d83e626b07b5038800100008040000340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c630700e40b5402040000340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c630700e40b5402"

        val builder = ExtrinsicBuilder(
            runtime = runtime,
            keypair = KEYPAIR,
            nonce = 34.toBigInteger(),
            runtimeVersion = RuntimeVersion(48, 4),
            genesisHash = jp.co.soramitsu.schema.extensions.fromHex(),
            encryptionType = EncryptionType.ED25519,
            accountIdentifier = multiAddressFromId(KEYPAIR.publicKey),
            era = Era.Mortal(64, 59),
            blockHash = jp.co.soramitsu.schema.extensions.fromHex()
        )

        repeat(2) {
            builder.transfer(
                recipientAccountId = jp.co.soramitsu.schema.extensions.fromHex(),
                amount = BigInteger("10000000000")
            )
        }

        val encoded = builder.build()

        assertEquals(extrinsicInHex, encoded)
    }
}