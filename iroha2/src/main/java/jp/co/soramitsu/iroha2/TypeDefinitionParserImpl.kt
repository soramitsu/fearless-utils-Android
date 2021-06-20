package jp.co.soramitsu.iroha2

import jp.co.soramitsu.schema.*
import jp.co.soramitsu.schema.definitions.types.Type
import jp.co.soramitsu.schema.definitions.types.TypeReference
import jp.co.soramitsu.schema.definitions.types.composite.Alias

class TypeDefinitionParserImpl : TypeDefinitionParser {

    private class Params(
        val types: Map<String, Any>,
        val typeResolver: DynamicTypeResolver,
        val typesBuilder: TypePresetBuilder
    )

    override fun parseBaseDefinitions(
        types: Map<String, Any>,
        typePreset: TypePreset,
        typeResolver: DynamicTypeResolver
    ): ParseResult {
        val builder = typePreset.newBuilder()
        val parsingParams: Params = Params(types, typeResolver, builder)

        for (name in types.keys) {
            val typeValue = types[name]
            val type = parseType(parsingParams, name, typeValue) ?: continue

            builder.type(type)
        }
        val unknownTypes = builder.entries
            .mapNotNull { (name, typeRef) -> if (!typeRef.isResolved()) name else null }

        return ParseResult(builder, unknownTypes)
    }

    private fun parseType(parsingParams: Params, name: String, typeValue: Any?): Type<*>? {
        return when (typeValue) {
            is String -> {
                val dynamicType = resolveDynamicType(parsingParams, name, typeValue)

                when {
                    dynamicType != null -> dynamicType
                    typeValue == name -> parsingParams.typesBuilder[name]?.value
                    else -> Alias(name, parsingParams.typesBuilder.getOrCreate(typeValue))
                }
            }

//            is Map<*, *> -> {
//                val typeValueCasted = typeValue as Map<String, Any?>
//
//                when (typeValueCasted["type"]) {
//                    TOKEN_STRUCT -> {
//                        val typeMapping = typeValueCasted["type_mapping"] as List<List<String>>
//                        val children = parseTypeMapping(parsingParams, typeMapping)
//
//                        Struct(name, children)
//                    }
//
//                    TOKEN_ENUM -> {
//                        val valueList = typeValueCasted["value_list"] as? List<String>
//                        val typeMapping = typeValueCasted["type_mapping"] as? List<List<String>>
//
//                        when {
//                            valueList != null -> CollectionEnum(name, valueList)
//
//                            typeMapping != null -> {
//                                val children = parseTypeMapping(parsingParams, typeMapping)
//                                    .map { (name, typeRef) -> DictEnum.Entry(name, typeRef) }
//
//                                DictEnum(name, children)
//                            }
//                            else -> null
//                        }
//                    }
//
//                    TOKEN_SET -> {
//                        val valueTypeName = typeValueCasted["value_type"] as String
//                        val valueListRaw = typeValueCasted["value_list"] as Map<String, Double>
//
//                        val valueTypeRef = resolveTypeAllWaysOrCreate(parsingParams, valueTypeName)
//
//                        val valueList = valueListRaw.mapValues { (_, value) ->
//                            BigInteger(value.toInt().toString())
//                        }
//
//                        SetType(name, valueTypeRef, LinkedHashMap(valueList))
//                    }
//
//                    else -> null
//                }
//            }

            else -> null
        }
    }

    private fun resolveDynamicType(
        parsingParams: Params,
        name: String,
        typeDef: String
    ): Type<*>? {
        return parsingParams.typeResolver.createDynamicType(name, typeDef) {
            resolveTypeAllWaysOrCreate(parsingParams, it)
        }
    }

    private fun resolveTypeAllWaysOrCreate(
        parsingParams: Params,
        typeDef: String,
        name: String = typeDef
    ): TypeReference {
        return parsingParams.typesBuilder[name]
            ?: resolveDynamicType(parsingParams, name, typeDef)?.let(::TypeReference)
            ?: parsingParams.typesBuilder.create(name)
    }

    private fun parseTypeMapping(
        parsingParams: Params,
        typeMapping: List<List<String>>
    ): LinkedHashMap<String, TypeReference> {
        val children = LinkedHashMap<String, TypeReference>()

        for ((fieldName, fieldType) in typeMapping) {
            children[fieldName] = resolveTypeAllWaysOrCreate(parsingParams, fieldType)
        }

        return children
    }
}
