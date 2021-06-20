package jp.co.soramitsu.fearless_utils.runtime

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import jp.co.soramitsu.fearless_utils.common.getFileContentFromResources
import jp.co.soramitsu.fearless_utils.common.getResourceReader
import jp.co.soramitsu.fearless_utils.runtime.definitions.TypeDefinitionParserImpl
import jp.co.soramitsu.fearless_utils.runtime.definitions.TypeDefinitionsTree
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.substratePreParsePreset
import jp.co.soramitsu.fearless_utils.runtime.metadata.RuntimeMetadata
import jp.co.soramitsu.fearless_utils.runtime.metadata.RuntimeMetadataSchema
import jp.co.soramitsu.schema.DynamicTypeResolver
import jp.co.soramitsu.schema.definitions.dynamic.extentsions.GenericsExtension

object RealRuntimeProvider {

    fun buildRuntime(networkName: String): RuntimeSnapshot {
        val metadataRaw = buildRawMetadata(networkName)

        return RuntimeSnapshot()
            .also { it.typeRegistry = buildRegistry(networkName, it)}
            .also { it.metadata = RuntimeMetadata(it.typeRegistry, metadataRaw) }
    }

    fun buildRawMetadata(networkName: String = "kusama") = getFileContentFromResources("${networkName}_metadata").run {
        RuntimeMetadataSchema.read(this)
    }

    fun buildRegistry(networkName: String, runtime: RuntimeSnapshot = RuntimeSnapshot()): TypeRegistry {
        val gson = Gson()
        val reader = JsonReader(getResourceReader("default.json"))
        val kusamaReader = JsonReader(getResourceReader("${networkName}.json"))

        val tree = gson.fromJson<TypeDefinitionsTree>(reader, TypeDefinitionsTree::class.java)
        val kusamaTree =
            gson.fromJson<TypeDefinitionsTree>(kusamaReader, TypeDefinitionsTree::class.java)

        val defaultTypeRegistry =
            TypeDefinitionParserImpl.parseBaseDefinitions(tree.types, substratePreParsePreset(runtime)).typePreset
        val networkParsed = TypeDefinitionParserImpl.parseNetworkVersioning(
            kusamaTree,
            defaultTypeRegistry
        )

        return TypeRegistry(
            types = networkParsed.typePreset,
            dynamicTypeResolver = DynamicTypeResolver(
                DynamicTypeResolver.DEFAULT_COMPOUND_EXTENSIONS + GenericsExtension
            )
        )
    }
}
