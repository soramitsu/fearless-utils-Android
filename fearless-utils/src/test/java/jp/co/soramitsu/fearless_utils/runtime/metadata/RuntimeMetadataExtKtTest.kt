package jp.co.soramitsu.fearless_utils.runtime.metadata

import jp.co.soramitsu.fearless_utils.common.assertThrows
import jp.co.soramitsu.fearless_utils.extensions.toHexString
import jp.co.soramitsu.fearless_utils.hash.Hasher.xxHash128
import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.BooleanType
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

private val MODULE_NAME = "Test".encodeToByteArray()
private val TEST_NAME = "Test".encodeToByteArray()

private val PREFIX =
    (MODULE_NAME.xxHash128() + TEST_NAME.xxHash128()).toHexString(withPrefix = true)

@RunWith(MockitoJUnitRunner::class)
class RuntimeMetadataExtKtTest {

    @Mock
    lateinit var runtime: RuntimeSnapshot

    @Test
    fun `test plain storage`() {
        val storageEntry = storageEntry(StorageEntryType.Plain(value = BooleanType))

        assertEquals(PREFIX, storageEntry.storageKey())

        assertThrows<IllegalArgumentException> {
            storageEntry.storageKey(runtime, false)
        }
    }

    @Test
    fun `test single map storage`() {
        val storageEntry = storageEntry(
            StorageEntryType.Map(
                value = BooleanType,
                key = BooleanType,
                hasher = StorageHasher.Identity,
                unused = false
            )
        )

        assertEquals(PREFIX, storageEntry.storageKey(runtime))
        assertEquals(PREFIX + "01", storageEntry.storageKey(runtime, true))

        assertThrows<IllegalArgumentException> {
            storageEntry.storageKey(runtime, false, false)
        }
    }

    @Test
    fun `test double map storage`() {
        val storageEntry = storageEntry(
            StorageEntryType.DoubleMap(
                value = BooleanType,
                key1 = BooleanType,
                key1Hasher = StorageHasher.Identity,
                key2 = BooleanType,
                key2Hasher = StorageHasher.Identity,
            )
        )

        assertEquals(PREFIX, storageEntry.storageKey(runtime))
        assertEquals(PREFIX + "01", storageEntry.storageKey(runtime, true))
        assertEquals(PREFIX + "0100", storageEntry.storageKey(runtime, true, false))

        assertThrows<IllegalArgumentException> {
            storageEntry.storageKey(runtime, false, false, false)
        }
    }

    @Test
    fun `test nmap`() {
        val storageEntry = storageEntry(
            StorageEntryType.NMap(
                value = BooleanType,
                keys = listOf(BooleanType, BooleanType, BooleanType),
                hashers = listOf(
                    StorageHasher.Identity,
                    StorageHasher.Identity,
                    StorageHasher.Identity
                )
            )
        )

        assertEquals(PREFIX, storageEntry.storageKey(runtime))
        assertEquals(PREFIX + "01", storageEntry.storageKey(runtime, true))
        assertEquals(PREFIX + "0100", storageEntry.storageKey(runtime, true, false))
        assertEquals(PREFIX + "010001", storageEntry.storageKey(runtime, true, false, true))

        assertThrows<IllegalArgumentException> {
            storageEntry.storageKey(runtime, false, false, false, false)
        }
    }

    private fun storageEntry(storageEntryType: StorageEntryType): StorageEntry {
        val mock = Mockito.mock(StorageEntry::class.java)

        Mockito.`when`(mock.type).thenReturn(storageEntryType)
        Mockito.`when`(mock.moduleName).thenReturn("Test")
        Mockito.`when`(mock.name).thenReturn("Test")

        return mock
    }
}