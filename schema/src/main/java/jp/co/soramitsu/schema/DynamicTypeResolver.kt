package jp.co.soramitsu.schema

import jp.co.soramitsu.schema.definitions.dynamic.DynamicTypeExtension
import jp.co.soramitsu.schema.definitions.dynamic.TypeProvider
import jp.co.soramitsu.schema.definitions.dynamic.extentsions.*
import jp.co.soramitsu.schema.definitions.types.Type
import jp.co.soramitsu.schema.extensions.tryFindNonNull

class DynamicTypeResolver(
    val extensions: List<DynamicTypeExtension>
) {
    constructor(vararg extensions: DynamicTypeExtension) : this(extensions.toList())

    companion object {
        fun defaultCompoundResolver(): DynamicTypeResolver {
            return DynamicTypeResolver(DEFAULT_COMPOUND_EXTENSIONS)
        }

        val DEFAULT_COMPOUND_EXTENSIONS = listOf(
            VectorExtension,
            CompactExtension,
            OptionExtension,
            BoxExtension,
            TupleExtension,
            FixedArrayExtension,
            HashMapExtension,
            ResultTypeExtension
        )
    }

    fun createDynamicType(
        name: String,
        typeDef: String,
        innerTypeProvider: TypeProvider
    ): Type<*>? {
        return extensions.tryFindNonNull {
            it.createType(
                name,
                typeDef,
                innerTypeProvider
            )
        }
    }
}