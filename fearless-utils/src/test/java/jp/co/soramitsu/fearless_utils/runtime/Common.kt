package jp.co.soramitsu.fearless_utils.runtime

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import jp.co.soramitsu.fearless_utils.common.getFileContentFromResources
import jp.co.soramitsu.fearless_utils.common.getResourceReader
import jp.co.soramitsu.fearless_utils.runtime.definitions.TypeDefinitionParser
import jp.co.soramitsu.fearless_utils.runtime.definitions.TypeDefinitionsTree
import jp.co.soramitsu.fearless_utils.runtime.definitions.dynamic.DynamicTypeResolver
import jp.co.soramitsu.fearless_utils.runtime.definitions.dynamic.extentsions.GenericsExtension
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.substratePreParsePreset
import jp.co.soramitsu.fearless_utils.runtime.metadata.RuntimeMetadata
import jp.co.soramitsu.fearless_utils.runtime.metadata.RuntimeMetadataSchema
import org.junit.Assert

object RealRuntimeProvider {

    fun buildRuntime(networkName: String): RuntimeSnapshot {
        val metadataRaw = buildRawMetadata(networkName)
        val typeRegistry = buildRegistry(networkName)

        val metadata = RuntimeMetadata(typeRegistry, metadataRaw)

        return RuntimeSnapshot(typeRegistry, metadata)
    }

    fun buildRawMetadata(networkName: String = "kusama") = getFileContentFromResources("${networkName}_metadata").run {
        RuntimeMetadataSchema.read(this)
    }

    fun buildRegistry(networkName: String): TypeRegistry {
        val gson = Gson()
        val reader = JsonReader(getResourceReader("default.json"))
        val kusamaReader = JsonReader(getResourceReader("${networkName}.json"))

        val tree = gson.fromJson<TypeDefinitionsTree>(reader, TypeDefinitionsTree::class.java)
        val kusamaTree =
            gson.fromJson<TypeDefinitionsTree>(kusamaReader, TypeDefinitionsTree::class.java)

        val defaultTypeRegistry =
            TypeDefinitionParser.parseBaseDefinitions(tree, substratePreParsePreset()).typePreset
        val networkParsed = TypeDefinitionParser.parseNetworkVersioning(
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