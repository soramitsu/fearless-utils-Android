package jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics

import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType
import jp.co.soramitsu.fearless_utils.extensions.fromHex
import jp.co.soramitsu.fearless_utils.extensions.toHexString
import jp.co.soramitsu.fearless_utils.runtime.RealRuntimeProvider
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.DictEnum
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.fromHex
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.toHex
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigInteger

class ExtrinsicTest {

    private val inHex = "0x45028400340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c630124d30c51db45d81caf59bdf795ca7d6122d47ec6f2979392d216ce51d551447d3ff3b61e0e2bbed3f8a19bf0199d1440f88f91ac04bcad6c67ad352456507782c500910100040300fdc41550fb5186d71cae699c31731b3e1baa10680c7bd6b3831a6d222cf4d1680700e40b5402"
    private val signatureInHex = "24d30c51db45d81caf59bdf795ca7d6122d47ec6f2979392d216ce51d551447d3ff3b61e0e2bbed3f8a19bf0199d1440f88f91ac04bcad6c67ad352456507782"

    val runtime = RealRuntimeProvider.buildRuntime("westend")

    @Test
    fun `should decode transfer extrinsic`() {
        val decoded = Extrinsic.fromHex(runtime, inHex)

        val multiSignature = decoded.signature!!.tryExtractMultiSignature()!!

        assertEquals(signatureInHex, multiSignature.value.toHexString())
    }

    @Test
    fun `should encode transfer extrinsic`() {
        val call = GenericCall.Instance(
            moduleIndex = 4,
            callIndex = 3,
            arguments = mapOf(
                "dest" to DictEnum.Entry(
                    name = "Id",
                    value = "fdc41550fb5186d71cae699c31731b3e1baa10680c7bd6b3831a6d222cf4d168".fromHex()
                ),
                "value" to BigInteger("10000000000")
            )
        )

        val signature = MultiSignature(EncryptionType.SR25519, signatureInHex.fromHex())

        val signedExtras = mapOf(
            SignedExtras.TIP to 0.toBigInteger(),
            SignedExtras.NONCE to 100.toBigInteger(),
            SignedExtras.ERA to Era.Mortal(64, 12)
        )

        val extrinsic = Extrinsic.Instance(
            signature = Extrinsic.ExtrinsicSignature.newV28(
                accountId = "340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c63".fromHex(),
                signature = signature,
                signedExtras = signedExtras
            ),
            call = call
        )

        val encoded = Extrinsic.toHex(runtime, extrinsic)

        assertEquals(inHex, encoded)
    }
}