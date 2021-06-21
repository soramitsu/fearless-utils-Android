package jp.co.soramitsu.iroha2

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.iroha2.schema.TypeDefinitionParserImpl.parseBaseDefinitions
import jp.co.soramitsu.schema.DynamicTypeResolver
import jp.co.soramitsu.schema.createTypePresetBuilder
import jp.co.soramitsu.schema.definitions.dynamic.DynamicTypeExtension
import jp.co.soramitsu.schema.definitions.dynamic.TypeProvider
import jp.co.soramitsu.schema.definitions.dynamic.extentsions.WrapperExtension
import jp.co.soramitsu.schema.definitions.types.Type
import jp.co.soramitsu.schema.definitions.types.TypeReference
import jp.co.soramitsu.schema.definitions.types.composite.Option
import jp.co.soramitsu.schema.definitions.types.composite.Vec
import java.nio.file.Files
import java.nio.file.Paths

fun main() {
    val gson = Gson()
    val reader = JsonReader(Files.newBufferedReader(Paths.get("/Users/pd/Desktop/schema.json")))

    val types = gson.fromJson<Map<String, Any>>(reader, Map::class.java)

    val typeResolver = DynamicTypeResolver(
        MapExtension,
        VectorExtension,
        OptionExtension
    )
    val result = parseBaseDefinitions(types, createTypePresetBuilder(), typeResolver)
    result.typePreset.forEach(::println)
    println("\n=============================================\n")
    result.unknownTypes.let { if (it.isNotEmpty()) it.forEach(::println) }
}

object MapExtension : DynamicTypeExtension {

    override fun createType(name: String, typeDef: String, typeProvider: TypeProvider): Type<*>? {
        if (!typeDef.startsWith("alloc::collections::BTreeMap")) return null
        val withoutBrackets = typeDef.removePrefix("alloc::collections::BTreeMap").removeSurrounding("<", ">")
        if (withoutBrackets.split(",").size != 2) return null
        val tuple = "($withoutBrackets)"
        val typeRef = typeProvider(tuple)
        return Vec("Vec<$tuple>", typeRef)
    }
}

object VectorExtension : WrapperExtension() {
    override val wrapperName = "alloc::vec::Vec"

    override fun createWrapper(name: String, innerTypeRef: TypeReference): Type<*> = Vec(name, innerTypeRef)
}

object OptionExtension : WrapperExtension() {
    override val wrapperName = "core::option::Option"

    override fun createWrapper(name: String, innerTypeRef: TypeReference) = Option(name, innerTypeRef)
}


@Suppress("UNCHECKED_CAST")
class TupleStruct(
    name: String,
    private val types: List<TypeReference>
) : Type<TupleStruct.Instance>(name) {

    class Instance(val mapping: Map<String, Any?>) {
        inline operator fun <reified R> get(key: String): R? = mapping[key] as? R
    }

    override fun decode(scaleCodecReader: ScaleCodecReader): Instance {
        TODO("Not yet implemented")
    }

    override fun isValidInstance(instance: Any?): Boolean {
        if (instance !is Instance) return false

        return types.all { type ->
            type.requireValue().isValidInstance(instance)
        }
    }

    override val isFullyResolved: Boolean
        get() = types.all { it.isResolved() }

    override fun encode(scaleCodecWriter: ScaleCodecWriter, value: Instance) {
        TODO("Not yet implemented")
    }
}


@Suppress("UNCHECKED_CAST")
class Enum(
    name: String,
    private val variants: List<Variant>
) : Type<Enum>(name) {

    class Variant(val name: String, val discriminant: Int, val type: TypeReference)

    override fun decode(scaleCodecReader: ScaleCodecReader): Enum {
        TODO("Not yet implemented")
    }

    override fun isValidInstance(instance: Any?): Boolean {
        if (instance !is Enum) return false

        return variants.all { it.type.requireValue().isValidInstance(instance) }
    }

    override val isFullyResolved: Boolean
        get() = variants.all { it.type.isResolved() }

    override fun encode(scaleCodecWriter: ScaleCodecWriter, value: Enum) {
        TODO("Not yet implemented")
    }
}
