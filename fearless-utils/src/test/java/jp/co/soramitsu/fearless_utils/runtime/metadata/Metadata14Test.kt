package jp.co.soramitsu.fearless_utils.runtime.metadata

import jp.co.soramitsu.fearless_utils.common.assertInstance
import jp.co.soramitsu.fearless_utils.getFileContentFromResources
import jp.co.soramitsu.fearless_utils.runtime.definitions.dynamic.DynamicTypeResolver
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.type
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.typePreset
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.Bytes
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.BooleanType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.u128
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.u16
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.u256
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.u32
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.u64
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.u8
import jp.co.soramitsu.fearless_utils.runtime.definitions.v14.TypesParserV14
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.StorageEntryType
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.RuntimeMetadataSchemaV14
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class Metadata14Test {

    @Test
    fun `should decode metadata types v14`() {
        val inHex = getFileContentFromResources("westend_metadata_v14")
        val metadataRaw = RuntimeMetadataReader.read(inHex)
        val parseResult = TypesParserV14.parse(
            metadataRaw.getSchema()[RuntimeMetadataSchemaV14.lookup],
            typePreset {
                type(BooleanType)
                type(u8)
                type(u16)
                type(u32)
                type(u64)
                type(u128)
                type(u256)
                type(Bytes)
            }
        )
        Assert.assertEquals(0, parseResult.unknownTypes.size)
    }

    @Test
    fun `should decode metadata v14`() {
        val inHex = getFileContentFromResources("westend_metadata_v14")
        val metadataRaw = RuntimeMetadataReader.read(inHex)
        val parseResult = TypesParserV14.parse(
            metadataRaw.getSchema()[RuntimeMetadataSchemaV14.lookup],
            typePreset {
                type(BooleanType)
                type(u8)
                type(u16)
                type(u32)
                type(u64)
                type(u128)
                type(u256)
                type(Bytes)
            }
        )
        val metadata = RuntimeMetadata(
            TypeRegistry(
                parseResult.typePreset,
                DynamicTypeResolver.defaultCompoundResolver()
            ), metadataRaw
        )

        assertInstance<StorageEntryType.Plain>(metadata.module("System").storage("Events").type)
        Assert.assertEquals(4 to 2, metadata.module("Balances").event("Transfer").index)
        Assert.assertEquals(4 to 0, metadata.module("Balances").call("transfer").index)
    }
}