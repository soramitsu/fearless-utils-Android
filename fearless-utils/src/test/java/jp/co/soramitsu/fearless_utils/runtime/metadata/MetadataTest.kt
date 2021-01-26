package jp.co.soramitsu.fearless_utils.runtime.metadata

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import jp.co.soramitsu.fearless_utils.common.getFileContentFromResources
import jp.co.soramitsu.fearless_utils.common.getResourceReader
import jp.co.soramitsu.fearless_utils.runtime.definitions.TypeDefinitionParser
import jp.co.soramitsu.fearless_utils.runtime.definitions.TypeDefinitionsTree
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
import jp.co.soramitsu.fearless_utils.scale.EncodableStruct
import jp.co.soramitsu.fearless_utils.scale.Schema
import jp.co.soramitsu.fearless_utils.scale.dataType.list
import org.junit.Test

data class Holder(val module: String, val type: String)

class MetadataTest {

    @Test
    fun `should decode metadata`() {
        val inHex = getFileContentFromResources("test_runtime_metadata")

        val metadata = RuntimeMetadata.read(inHex)

        val gson = Gson()
        val reader = JsonReader(getResourceReader("default.json"))
        val tree = gson.fromJson<TypeDefinitionsTree>(reader, TypeDefinitionsTree::class.java)

        val parser = TypeDefinitionParser()

        val typeRegistry = parser.parseTypeDefinitions(tree).typeRegistry

        val toResolve = mutableSetOf<Holder>()

        for (module in metadata[RuntimeMetadata.modules]) {
            val toResolveInModule = mutableSetOf<String>()

            val storage = module[ModuleMetadata.storage]

            storage?.let {
                for (storageItem in storage[StorageMetadata.entries]) {
                    val toResolveStorage = when(val item = storageItem[StorageEntryMetadata.type]) {
                        is String -> listOf(item)
                        is EncodableStruct<*> -> when(item.schema) {
                            Map -> listOf(item[Map.key], item[Map.value])
                            DoubleMap -> listOf(item[DoubleMap.key1], item[DoubleMap.key2], item[DoubleMap.value])
                            else -> listOf()
                        }
                        else -> listOf()
                    }

                    toResolveInModule += toResolveStorage
                }
            }

            val calls = module[ModuleMetadata.calls]

            calls?.let {
                for (call in calls) {
                    for (argument in call[FunctionMetadata.arguments]) {
                        toResolveInModule += argument[FunctionArgumentMetadata.type]
                    }
                }
            }

            val events = module[ModuleMetadata.events]

            events?.let {
                for (event in events) {
                    toResolveInModule += event[EventMetadata.arguments]
                }
            }

            for (const in module[ModuleMetadata.constants]) {
                toResolveInModule += const[ModuleConstantMetadata.type]
            }

            toResolve += toResolveInModule.map { Holder(module[ModuleMetadata.name], it) }
        }

        val notResolvable = toResolve.filter { typeRegistry[clearType(it.type)] == null }

        println("To resolve: ${toResolve.size}, Resolved: ${(toResolve - notResolvable).size}, Not resolved: ${notResolvable.size}")

        notResolvable.groupBy { it.module }
            .forEach { (module, types) ->
                println("$module: ${types.map(Holder::type)}\n")
            }
    }

    private fun clearType(type: String) : String {
        return type.replace("(T::)|(<T>)|(<T as Trait>::)|(<T as Config>::)|(\n)|((grandpa|session|slashing)::)".toRegex(), "")
    }
}