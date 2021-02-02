package jp.co.soramitsu.fearless_utils.runtime.metadata

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import jp.co.soramitsu.fearless_utils.common.getFileContentFromResources
import jp.co.soramitsu.fearless_utils.common.getResourceReader
import jp.co.soramitsu.fearless_utils.runtime.definitions.TypeDefinitionParser
import jp.co.soramitsu.fearless_utils.runtime.definitions.TypeDefinitionsTree
import jp.co.soramitsu.fearless_utils.runtime.definitions.dynamic.DynamicTypeResolver
import jp.co.soramitsu.fearless_utils.runtime.definitions.dynamic.extentsions.GenericsExtension
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.kusamaExtrasPreset
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.substratePreParsePreset
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.stub.FakeType
import jp.co.soramitsu.fearless_utils.scale.EncodableStruct
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

data class Holder(val module: String, val type: String)

@RunWith(MockitoJUnitRunner::class)
class MetadataTest {

    @Mock
    private lateinit var typeRegistry: TypeRegistry

    @Before
    fun startUp() {
        Mockito.`when`(typeRegistry[Mockito.anyString()])
            .thenAnswer { FakeType(it.arguments[0] as String) }
    }

    @Test
    fun `should decode metadata`() {
        val inHex = getFileContentFromResources("test_runtime_metadata")

        val metadataRaw = RuntimeMetadataSchema.read(inHex)
        val metadata = RuntimeMetadata(typeRegistry, metadataRaw)
    }

    @Test
//    @Ignore("Manual run")
    fun `connect metadata with real type registry`() {
        val metadataRaw = buildRawMetadata()
        val kusamaTypeRegistry = buildKusamaRegistry()

        val metadata = RuntimeMetadata(kusamaTypeRegistry, metadataRaw)

        print(metadata)
    }

    @Test
    @Ignore("Manual run")
    fun `find unknown types in metadata`() {
        val metadata = buildRawMetadata()
        val kusamaTypeRegistry = buildKusamaRegistry()

        val toResolve = mutableSetOf<Holder>()

        for (module in metadata[RuntimeMetadataSchema.modules]) {
            val toResolveInModule = mutableSetOf<String>()

            val storage = module[ModuleMetadataSchema.storage]

            storage?.let {
                for (storageItem in storage[StorageMetadataSchema.entries]) {
                    val toResolveStorage =
                        when (val item = storageItem[StorageEntryMetadataSchema.type]) {
                            is String -> listOf(item)
                            is EncodableStruct<*> -> when (item.schema) {
                                MapSchema -> listOf(item[MapSchema.key], item[MapSchema.value])
                                DoubleMapSchema -> listOf(
                                    item[DoubleMapSchema.key1],
                                    item[DoubleMapSchema.key2],
                                    item[DoubleMapSchema.value]
                                )
                                else -> listOf()
                            }
                            else -> listOf()
                        }

                    toResolveInModule += toResolveStorage
                }
            }

            val calls = module[ModuleMetadataSchema.calls]

            calls?.let {
                for (call in calls) {
                    for (argument in call[FunctionMetadataSchema.arguments]) {
                        toResolveInModule += argument[FunctionArgumentMetadataSchema.type]
                    }
                }
            }

            val events = module[ModuleMetadataSchema.events]

            events?.let {
                for (event in events) {
                    toResolveInModule += event[EventMetadataSchema.arguments]
                }
            }

            for (const in module[ModuleMetadataSchema.constants]) {
                toResolveInModule += const[ModuleConstantMetadataSchema.type]
            }

            toResolve += toResolveInModule.map { Holder(module[ModuleMetadataSchema.name], it) }
        }

        val notResolvable =
            toResolve.filter { kusamaTypeRegistry[it.type]?.isFullyResolved?.not() ?: true }

        println("To resolve: ${toResolve.size}, Resolved: ${(toResolve - notResolvable).size}, Not resolved: ${notResolvable.size}")

        notResolvable.groupBy { it.module }
            .forEach { (module, types) ->
                println("$module: ${types.map(Holder::type)}\n")
            }
    }

    private fun buildRawMetadata() = getFileContentFromResources("test_runtime_metadata").run {
        RuntimeMetadataSchema.read(this)
    }

    private fun buildKusamaRegistry(): TypeRegistry {
        val gson = Gson()
        val reader = JsonReader(getResourceReader("default.json"))
        val kusamaReader = JsonReader(getResourceReader("kusama.json"))

        val tree = gson.fromJson<TypeDefinitionsTree>(reader, TypeDefinitionsTree::class.java)
        val kusamaTree =
            gson.fromJson<TypeDefinitionsTree>(kusamaReader, TypeDefinitionsTree::class.java)

        val defaultTypeRegistry =
            TypeDefinitionParser.parseTypeDefinitions(tree, substratePreParsePreset()).typePreset
        val typesPreset = TypeDefinitionParser.parseTypeDefinitions(
            kusamaTree,
            defaultTypeRegistry + kusamaExtrasPreset()
        ).typePreset

        return TypeRegistry(
            types = typesPreset,
            dynamicTypeResolver = DynamicTypeResolver(
                DynamicTypeResolver.DEFAULT_COMPOUND_EXTENSIONS + GenericsExtension
            )
        )
    }
}
