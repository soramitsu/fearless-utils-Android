package jp.co.soramitsu.fearless_utils.runtime.extrinsic

import jp.co.soramitsu.fearless_utils.encrypt.keypair.BaseKeypair
import jp.co.soramitsu.fearless_utils.extensions.fromHex
import jp.co.soramitsu.fearless_utils.runtime.RealRuntimeProvider
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.fromHex
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.Extrinsic
import jp.co.soramitsu.fearless_utils.ss58.SS58Encoder.toAddress
import org.junit.Assert.assertEquals
import org.junit.Test

class ExtrinsicSora14Test {

    private val soraRuntime = RealRuntimeProvider.buildRuntime("sora2", "_v14")

    private val soraKeypair = BaseKeypair(
        privateKey = "fb981a1fe2aeb2c6f3eaf50af5ac2408b6f5ab4b04ae124aa4546d71014d6e0d".fromHex(),
        publicKey = "bcc5ecf679ebd776866a04c212a4ec5dc45cefab57d7aa858c389844e212693f".fromHex()
    )

    @Test
    fun `sora parse storage to get events for transfer extrinsic`() {
        val extrinsic =
            "0xe10284bcc5ecf679ebd776866a04c212a4ec5dc45cefab57d7aa858c389844e212693f0188d5209090f899c8745ce97fa7a0ac7a1dd4863a7cb78370e9f5d8b89d1c081543bb2c78d88eb05fc240b4e0238645f580d82ca7e5dba01a692c9461eda7398795033c00150102000400000000000000000000000000000000000000000000000000000000008c7eef30ac094c2b3ad9c1297dc1d2c8a2bb4d9085c7a7bff6f82cc4b9eae6420000f444829163450000000000000000"
        val encoded = Extrinsic.fromHex(soraRuntime, extrinsic)

        val address = (encoded.signature?.accountIdentifier as? ByteArray)?.toAddress(69)
        assertEquals("cnVkoGs3rEMqLqY27c2nfVXJRGdzNJk2ns78DcqtppaSRe8qm", address)
        assertEquals("Assets", encoded.call.module.name)
        assertEquals("transfer", encoded.call.function.name)
        assertEquals(3, encoded.call.arguments.size)
    }
}
