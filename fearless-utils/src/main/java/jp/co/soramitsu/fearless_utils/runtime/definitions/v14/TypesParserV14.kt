package jp.co.soramitsu.fearless_utils.runtime.definitions.v14

import jp.co.soramitsu.fearless_utils.runtime.definitions.ParseResult
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
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.DictEnum
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.FixedArray
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Tuple
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Vec
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.Bytes
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.Compact
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.LookupSchema
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.PortableType
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.RegistryType
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.TypeDefArray
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.TypeDefBitSequence
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.TypeDefCompact
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.TypeDefComposite
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.TypeDefCompositeField
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.TypeDefEnum
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.TypeDefSequence
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.TypeDefVariant
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.TypeDefVariantItem
import jp.co.soramitsu.fearless_utils.scale.EncodableStruct
import java.math.BigInteger

object TypesParserV14 {

    private class Params(
        val types: List<EncodableStruct<PortableType>>,
        val dynamicTypeResolver: DynamicTypeResolver,
        val typesBuilder: TypePresetBuilder
    )

    fun parse(
        struct: EncodableStruct<LookupSchema>,
        typePreset: TypePreset,
        dynamicTypeResolver: DynamicTypeResolver = DynamicTypeResolver.defaultCompoundResolver()
    ): ParseResult {
        val builder = typePreset.newBuilder()
        val params = Params(struct[LookupSchema.types], dynamicTypeResolver, builder)
        parseParams(params)
        val unknownTypes = params.typesBuilder.entries
            .mapNotNull { (name, typeRef) -> if (!typeRef.isResolved()) name else null }
        return ParseResult(params.typesBuilder, unknownTypes)
    }

    private fun parseParams(params: Params) {
        for (type in params.types) {
            val t = parseParam(params, type) ?: continue
            params.typesBuilder.type(t)
        }
    }

    private fun parseParam(params: Params, pt: EncodableStruct<PortableType>): Type<*>? {
        val typesBuilder = params.typesBuilder
        val name = pt[PortableType.id]
        val type = pt[PortableType.type]
        val def = type[RegistryType.def]
        return when (def) {
            is EncodableStruct<*> -> {
                when {
                    def.schema is TypeDefComposite -> {
                        val list = def[TypeDefComposite.fields2]
                        val children = parseTypeMapping(params, list as List<*>)
                        Struct(name.toString(), children)
                    }
                    def.schema is TypeDefArray -> {
                        FixedArray(
                            name.toString(),
                            def[TypeDefArray.len].toInt(),
                            resolveTypeAllWaysOrCreate(params, def[TypeDefArray.type].toString())
                        )
                    }
                    def.schema is TypeDefSequence -> {
                        Vec(
                            name.toString(),
                            resolveTypeAllWaysOrCreate(params, def[TypeDefSequence.type].toString())
                        )
                    }
                    def.schema is TypeDefVariant -> {
                        val list = def[TypeDefVariant.variants]
                        val res = list.map {
                            val ch = it[TypeDefVariantItem.fields2]
                            val itemName = it[TypeDefVariantItem.index].toString()
                            val children = parseTypeMapping(params, ch as List<*>)
                            val s = Struct(itemName, children)
                            DictEnum.Entry(it[TypeDefVariantItem.name], TypeReference(s))
                        }
                        DictEnum(name.toString(), res)
                    }
                    def.schema is TypeDefCompact -> {
                        Compact(name.toString())
                    }
                    def.schema is TypeDefBitSequence -> {
                        Tuple(
                            name.toString(),
                            listOf(
                                def[TypeDefBitSequence.bit_store_type],
                                def[TypeDefBitSequence.bit_order_type]
                            ).map { resolveTypeAllWaysOrCreate(params, it.toString()) }
                        )
                    }
                    else -> {
                        null
                    }
                }
            }
            is TypeDefEnum -> {
                when (def) {
                    TypeDefEnum.str -> {
                        Alias(name.toString(), TypeReference(Bytes))
                    }
                    TypeDefEnum.char -> {
                        Alias(name.toString(), TypeReference(Bytes))
                    }
                    else -> {
                        Alias(name.toString(), typesBuilder.getOrCreate(def.localName))
                    }
                }
            }
            is List<*> -> {
                (def as? List<BigInteger>)?.let { list ->
                    Tuple(
                        name.toString(),
                        list.map { resolveTypeAllWaysOrCreate(params, it.toString()) }
                    )
                }
            }
            else -> {
                null
            }
        }
    }

    private fun parseTypeMapping(
        params: Params,
        struct: List<*>,
    ): LinkedHashMap<String, TypeReference> {
        val children = LinkedHashMap<String, TypeReference>()
        for (type in struct) {
            val s = type as EncodableStruct<*>
            when {
                s.schema is TypeDefCompositeField -> {
                    val entryName =
                        s[TypeDefCompositeField.name] ?: s[TypeDefCompositeField.typeName]
                            ?: s[TypeDefCompositeField.type].toString()
                    children[entryName] =
                        resolveTypeAllWaysOrCreate(params, s[TypeDefCompositeField.type].toString())
                }
            }
        }
        return children
    }

    private fun resolveDynamicType(
        parsingParams: Params,
        name: String,
        typeDef: String
    ): Type<*>? {
        return parsingParams.dynamicTypeResolver.createDynamicType(name, typeDef) {
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
}
