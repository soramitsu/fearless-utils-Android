package jp.co.soramitsu.fearless_utils.runtime.definitions

import jp.co.soramitsu.fearless_utils.runtime.definitions.dynamic.DynamicTypeResolver
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypePreset
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypePresetBuilder
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.create
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.getOrCreate
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.newBuilder
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.type
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.TypeReference
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Alias
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.CollectionEnum
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.DictEnum
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.SetType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
import java.math.BigInteger

class TypeDefinitionsTree(val types: Map<String, Any>)

class ParseResult(
    val typePreset: TypePreset,
    val unknownTypes: List<String>
)

private const val TOKEN_SET = "set"
private const val TOKEN_STRUCT = "struct"
private const val TOKEN_ENUM = "enum"

object TypeDefinitionParser {

    private val dynamicTypeResolver = DynamicTypeResolver.defaultCompoundResolver()

    class Params(
        val tree: TypeDefinitionsTree,
        val typesBuilder: TypePresetBuilder
    )

    fun parseTypeDefinitions(
        tree: TypeDefinitionsTree,
        typePreset: TypePreset
    ): ParseResult {
        val builder = typePreset.newBuilder()

        val params = Params(tree, builder)

        for (name in tree.types.keys) {
            val type = parse(params, name) ?: continue

            params.typesBuilder.type(type)
        }

        val unknownTypes = params.typesBuilder.entries
            .mapNotNull { (name, typeRef) -> if (!typeRef.isResolved()) name else null }

        return ParseResult(params.typesBuilder, unknownTypes)
    }

    private fun parse(parsingParams: Params, name: String): Type<*>? {
        val typeValue = parsingParams.tree.types[name]

        return parseType(parsingParams, name, typeValue)
    }

    private fun parseType(parsingParams: Params, name: String, typeValue: Any?): Type<*>? {
        val typesBuilder = parsingParams.typesBuilder

        return when (typeValue) {
            is String -> {
                val dynamicType = resolveDynamicType(typesBuilder, name, typeValue)

                when {
                    dynamicType != null -> dynamicType
                    typeValue == name -> parsingParams.typesBuilder[name]?.value
                    else -> Alias(name, typesBuilder.getOrCreate(typeValue))
                }
            }

            is Map<*, *> -> {
                val typeValueCasted = typeValue as Map<String, Any?>

                when (typeValueCasted["type"]) {
                    TOKEN_STRUCT -> {
                        val typeMapping = typeValueCasted["type_mapping"] as List<List<String>>
                        val children = parseTypeMapping(typesBuilder, typeMapping)

                        Struct(name, children)
                    }

                    TOKEN_ENUM -> {
                        val valueList = typeValueCasted["value_list"] as? List<String>
                        val typeMapping = typeValueCasted["type_mapping"] as? List<List<String>>

                        when {
                            valueList != null -> CollectionEnum(name, valueList)

                            typeMapping != null -> {
                                val children =
                                    parseTypeMapping(
                                        typesBuilder,
                                        typeMapping
                                    ).map { (name, typeRef) ->
                                        DictEnum.Entry(name, typeRef)
                                    }

                                DictEnum(name, children)
                            }
                            else -> null
                        }
                    }

                    TOKEN_SET -> {
                        val valueTypeName = typeValueCasted["value_type"] as String
                        val valueListRaw = typeValueCasted["value_list"] as Map<String, Double>

                        val valueTypeRef = resolveTypeAllWaysOrCreate(typesBuilder, valueTypeName)

                        val valueList = valueListRaw.mapValues { (_, value) ->
                            BigInteger(value.toInt().toString())
                        }

                        SetType(name, valueTypeRef, LinkedHashMap(valueList))
                    }

                    else -> null
                }
            }

            else -> null
        }
    }

    private fun parseTypeMapping(
        typesBuilder: TypePresetBuilder,
        typeMapping: List<List<String>>
    ): LinkedHashMap<String, TypeReference> {
        val children = LinkedHashMap<String, TypeReference>()

        for ((fieldName, fieldType) in typeMapping) {
            children[fieldName] = resolveTypeAllWaysOrCreate(typesBuilder, fieldType)
        }

        return children
    }

    private fun resolveDynamicType(
        typesBuilder: TypePresetBuilder,
        name: String,
        typeDef: String
    ): Type<*>? {
        return dynamicTypeResolver.createDynamicType(name, typeDef) {
            resolveTypeAllWaysOrCreate(typesBuilder, it)
        }
    }

    private fun resolveTypeAllWaysOrCreate(
        typesBuilder: TypePresetBuilder,
        typeDef: String,
        name: String = typeDef
    ): TypeReference {
        return typesBuilder[name]
            ?: resolveDynamicType(typesBuilder, name, typeDef)?.let(::TypeReference)
            ?: typesBuilder.create(name)
    }
}