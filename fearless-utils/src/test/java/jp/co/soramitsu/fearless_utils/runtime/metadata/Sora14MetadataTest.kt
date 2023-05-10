package jp.co.soramitsu.fearless_utils.runtime.metadata

import jp.co.soramitsu.fearless_utils.encrypt.keypair.BaseKeypair
import jp.co.soramitsu.fearless_utils.extensions.fromHex
import jp.co.soramitsu.fearless_utils.hash.Hasher.xxHash64
import jp.co.soramitsu.fearless_utils.hash.hashConcat
import jp.co.soramitsu.fearless_utils.runtime.RealRuntimeProvider
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.fromHex
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.StorageEntryType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigInteger

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

    @Test
    fun `dex manager storage 1`() {
        val storage = soraRuntime.metadata.module("DEXManager").storage("DEXInfos")
        val key = storage.storageKey(soraRuntime, BigInteger.ONE)
        val hex = "0xa1bd2c8b755a708aa525cd47c8e225fd49e90400771bdeb88bf8ecd95a3c447d5153cb1f00942ff401000000"
        assertEquals(hex, key)
    }

    @Test
    fun `dex manager storage 0`() {
        val storage = soraRuntime.metadata.module("DEXManager").storage("DEXInfos")
        val key = storage.storageKey(soraRuntime, BigInteger.ZERO)
        val hex = "0xa1bd2c8b755a708aa525cd47c8e225fd49e90400771bdeb88bf8ecd95a3c447db4def25cfda6ef3a00000000"
        assertEquals(hex, key)
    }

    @Test
    fun `dex manager storage 2`() {
        val storage = soraRuntime.metadata.module("DEXManager").storage("DEXInfos")
        val key = storage.storageKey(soraRuntime, BigInteger.TWO)
        val hex = "0xa1bd2c8b755a708aa525cd47c8e225fd49e90400771bdeb88bf8ecd95a3c447d9eb2dcce60f37a2702000000"
        assertEquals(hex, key)
    }

    @Test
    fun `sora2 substrate4 events parsing`() {
        val eventsInHex = "0x24000000000000004216c63db90f02000000010000000208bcc5ecf679ebd776866a04c212a4ec5dc45cefab57d7aa858c389844e212693f0000470ea1b0f80000000000000000000000010000000202bcc5ecf679ebd776866a04c212a4ec5dc45cefab57d7aa858c389844e212693f84bdc405d139399bba3ccea5d3de23316c9deeab661f57e2f4d1720cc66498590000b0d86b9088a600000000000000000000010000001501bcc5ecf679ebd776866a04c212a4ec5dc45cefab57d7aa858c389844e212693f84bdc405d139399bba3ccea5d3de23316c9deeab661f57e2f4d1720cc664985902000000000000000000000000000000000000000000000000000000000000000000b0d86b9088a600000000000000000000010000000900bcc5ecf679ebd776866a04c212a4ec5dc45cefab57d7aa858c389844e212693f0000470ea1b0f8000000000000000000000001000000020784bdc405d139399bba3ccea5d3de23316c9deeab661f57e2f4d1720cc664985900806d8176de180000000000000000000000010000000901bcc5ecf679ebd776866a04c212a4ec5dc45cefab57d7aa858c389844e212693f84bdc405d139399bba3ccea5d3de23316c9deeab661f57e2f4d1720cc664985900806d8176de180000000000000000000000010000000500bcc5ecf679ebd776866a04c212a4ec5dc45cefab57d7aa858c389844e212693f00f88918b5912c01000000000000000000000000000000000000000000000000000001000000000022c5d01c00000000"
        val eventType = soraRuntime.metadata.module("System").storage("Events").type.value!!
        val events = eventType.fromHex(soraRuntime, eventsInHex)
        assertTrue(events is List<*>)
        assertEquals(9, (events as? List<*>)?.size)
    }
}