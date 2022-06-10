package jp.co.soramitsu.fearless_utils.runtime.metadata

import jp.co.soramitsu.fearless_utils.encrypt.keypair.BaseKeypair
import jp.co.soramitsu.fearless_utils.extensions.fromHex
import jp.co.soramitsu.fearless_utils.runtime.RealRuntimeProvider
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
import org.junit.Assert.assertEquals
import org.junit.Test

class Sora14MetadataTest {

    private val soraRuntime = RealRuntimeProvider.buildRuntime("sora2", "_v14")

    private val soraKeypair = BaseKeypair(
        privateKey = "fb981a1fe2aeb2c6f3eaf50af5ac2408b6f5ab4b04ae124aa4546d71014d6e0d".fromHex(),
        publicKey = "bcc5ecf679ebd776866a04c212a4ec5dc45cefab57d7aa858c389844e212693f".fromHex()
    )

    private val t1 =
        "0x0200000000000000000000000000000000000000000000000000000000000000".fromHex().toList()
            .map { it.toInt().toBigInteger() }
    private val t2 =
        "0x0200050000000000000000000000000000000000000000000000000000000000".fromHex().toList()
            .map { it.toInt().toBigInteger() }

    private val hex =
        "0x7c2f67164deafeedd91e34da0331ade460c9ab7384f36f3de79a685fa22b44919d7224862f5243be3cf3be5853f2c810020000000000000000000000000000000000000000000000000000000000000013dd3ce42378c5cfe6a72f2938ba393d0200050000000000000000000000000000000000000000000000000000000000"

    @Test
    fun `sora parse storage to get events for transfer extrinsic`() {
        val storage = soraRuntime.metadata.module("PoolXYK").storage("Reserves")
        val e1 = Struct.Instance(mapOf("code" to t1))
        val e2 = Struct.Instance(mapOf("code" to t2))
        val key = storage.storageKey(soraRuntime, e1, e2)

        assertEquals(hex, key)
    }
}