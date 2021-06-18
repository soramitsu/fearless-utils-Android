package jp.co.soramitsu

import jp.co.soramitsu.schema.DynamicTypeResolver
import jp.co.soramitsu.schema.ParseResult
import jp.co.soramitsu.schema.TypeDefinitionParser
import jp.co.soramitsu.schema.TypePreset

class TypeDefinitionParserImpl : TypeDefinitionParser {
    override fun parseBaseDefinitions(
        types: Map<String, Any>,
        typePreset: TypePreset,
        dynamicTypeResolver: DynamicTypeResolver
    ): ParseResult {
        TODO("Not yet implemented")
    }

}