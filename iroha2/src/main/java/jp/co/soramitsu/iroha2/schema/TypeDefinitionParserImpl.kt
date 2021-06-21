package jp.co.soramitsu.iroha2.schema

import jp.co.soramitsu.iroha2.Enum
import jp.co.soramitsu.iroha2.TupleStruct
import jp.co.soramitsu.schema.*
import jp.co.soramitsu.schema.definitions.types.Type
import jp.co.soramitsu.schema.definitions.types.TypeReference
import jp.co.soramitsu.schema.definitions.types.composite.DictEnum
import jp.co.soramitsu.schema.definitions.types.composite.Struct

object TypeDefinitionParserImpl : TypeDefinitionParser {

    private class Params(
        val typeResolver: DynamicTypeResolver,
        val typesBuilder: TypePresetBuilder
    )

    override fun parseBaseDefinitions(
        types: Map<String, Any>,
        typePreset: TypePreset,
        dynamicTypeResolver: DynamicTypeResolver
    ): ParseResult {
        val builder = typePreset.newBuilder()
        val parsingParams = Params(dynamicTypeResolver, builder)

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
                return resolveDynamicType(parsingParams, name, typeValue)
                    ?: return parsingParams.typesBuilder.getOrCreate(typeValue).value
            }
            is Map<*, *> -> {
                when {
                    typeValue["Map"] != null || typeValue["Vec"] != null || typeValue["Option"] != null -> {
                        resolveDynamicType(parsingParams, name, name)
                            ?: parsingParams.typesBuilder.getOrCreate(name).value
                    }
                    typeValue["NamedStruct"] != null -> {
                        val components =
                            (typeValue["NamedStruct"] as Map<String, List<Map<String, String>>>)["declarations"]!!
                        val children = parseTypeMapping(parsingParams, components)
                        Struct(name, children)
                    }
                    typeValue["UnnamedStruct"] != null -> {
                        val components = (typeValue["UnnamedStruct"] as Map<String, List<String>>)["types"]!!
                        val children = components.map { resolveTypeAllWaysOrCreate(parsingParams, it) }
                        TupleStruct(name, children)
                    }
                    typeValue["Enum"] != null -> {
                        val components = (typeValue["Enum"] as Map<String, List<Map<String, Any>>>)["variants"]!!
                        val variants = components.map {
                            val ty = resolveDynamicType(parsingParams, name, name)?.let(::TypeReference)
                                ?: parsingParams.typesBuilder.getOrCreate(name)
                            Enum.Variant(
                                it["name"]!! as String,
                                (it["discriminant"]!! as Double).toInt(),
                                ty
                            )
                        }
                        Enum(name, variants)
                    }
                        typeValue["Int"] != null -> {
                        return null
                    }
                    else -> {
                        throw RuntimeException("Unexpected type $typeValue")
                    }
                }
            }
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
    typeMapping: List<Map<String,String>>
): LinkedHashMap<String, TypeReference> {
    val children = LinkedHashMap<String, TypeReference>()

    for (singleMapping in typeMapping) {
        for ((fieldName, fieldType) in singleMapping) {
            children[fieldName] = resolveTypeAllWaysOrCreate(parsingParams, fieldType)
        }
    }

    return children
}
}
