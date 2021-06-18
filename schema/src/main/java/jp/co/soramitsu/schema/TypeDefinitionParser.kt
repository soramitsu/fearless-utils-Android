package jp.co.soramitsu.schema

import jp.co.soramitsu.schema.definitions.types.TypeReference

interface TypeDefinitionParser {
    fun parseBaseDefinitions(
        types: Map<String, Any>,
        typePreset: TypePreset,
        dynamicTypeResolver: DynamicTypeResolver = DynamicTypeResolver.defaultCompoundResolver()
    ) : ParseResult
}

class ParseResult(
    val typePreset: TypePreset,
    val unknownTypes: List<String>
)

typealias TypePreset = Map<String, TypeReference>
