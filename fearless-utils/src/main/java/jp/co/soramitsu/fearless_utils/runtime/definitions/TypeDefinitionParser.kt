package jp.co.soramitsu.fearless_utils.runtime.definitions

import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.copy
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.substratePreParsePreset
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
    val typeRegistry: TypeRegistry,
    val unknownTypes: List<String>
)

private const val TOKEN_SET = "set"
private const val TOKEN_STRUCT = "struct"
private const val TOKEN_ENUM = "enum"

// 1. we don't want extensions to process complex types that explicitly defined (storageOnly = true)
// 2. we want to keep original structure of types while parsing (resolveAliasing = false)
private fun TypeRegistry.getForParsing(typeDef: String) = getTypeReference(typeDef, resolveAliasing = false, storageOnly = true)

object TypeDefinitionParser {

    class Params(
        val tree: TypeDefinitionsTree,
        val typeRegistry: TypeRegistry
    )

    fun parseTypeDefinitions(
        tree: TypeDefinitionsTree,
        prepopulatedTypeRegistry: TypeRegistry = substratePreParsePreset()
    ): ParseResult {
        val params = Params(tree, prepopulatedTypeRegistry.copy())

        for (name in tree.types.keys) {
            val type = parse(params, name) ?: continue

            params.typeRegistry.registerType(type)
        }

        val unknownTypes = params.typeRegistry.allTypeRefs()
            .mapNotNull { (name, typeRef) -> if (!typeRef.isResolved()) name else null }

        return ParseResult(params.typeRegistry, unknownTypes)
    }

    private fun parse(parsingParams: Params, name: String): Type<*>? {
        val typeValue = parsingParams.tree.types[name]

        return parseType(parsingParams, name, typeValue)
    }

    private fun parseType(parsingParams: Params, name: String, typeValue: Any?): Type<*>? {
        val typeRegistry = parsingParams.typeRegistry

        return when (typeValue) {
            is String -> {
                val dynamicType = typeRegistry.resolveFromExtensions(name, typeValue)

                when {
                    dynamicType != null -> dynamicType
                    typeValue == name -> typeRegistry.getForParsing(name).value
                    else -> Alias(
                        name,
                        typeRegistry.getForParsing(typeValue)
                    )
                }
            }

            is Map<*, *> -> {
                val typeValueCasted = typeValue as Map<String, Any?>

                when (typeValueCasted["type"]) {
                    TOKEN_STRUCT -> {
                        val typeMapping = typeValueCasted["type_mapping"] as List<List<String>>
                        val children = parseTypeMapping(typeRegistry, typeMapping)

                        Struct(name, children)
                    }

                    TOKEN_ENUM -> {
                        val valueList = typeValueCasted["value_list"] as? List<String>
                        val typeMapping = typeValueCasted["type_mapping"] as? List<List<String>>

                        when {
                            valueList != null -> CollectionEnum(name, valueList)

                            typeMapping != null -> {
                                val children =
                                    parseTypeMapping(typeRegistry, typeMapping).map { (name, typeRef) ->
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

                        val valueTypeRef = typeRegistry.getTypeReference(valueTypeName, resolveAliasing = false)

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

    private fun parseTypeMapping(typeRegistry: TypeRegistry, typeMapping: List<List<String>>): LinkedHashMap<String, TypeReference> {
        val children = LinkedHashMap<String, TypeReference>()

        for ((fieldName, fieldType) in typeMapping) {

            // resolveAliasing = false to keep original type structure
            val typeRef = typeRegistry.getTypeReference(fieldType, resolveAliasing = false)

            children[fieldName] = typeRef
        }

        return children
    }
}
