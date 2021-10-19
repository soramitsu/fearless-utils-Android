package jp.co.soramitsu.fearless_utils.runtime.metadata.module

import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Tuple
import jp.co.soramitsu.fearless_utils.runtime.metadata.DoubleMapSchema
import jp.co.soramitsu.fearless_utils.runtime.metadata.ErrorMetadataSchema
import jp.co.soramitsu.fearless_utils.runtime.metadata.EventMetadataSchema
import jp.co.soramitsu.fearless_utils.runtime.metadata.FunctionArgumentMetadataSchema
import jp.co.soramitsu.fearless_utils.runtime.metadata.FunctionMetadataSchema
import jp.co.soramitsu.fearless_utils.runtime.metadata.MapSchema
import jp.co.soramitsu.fearless_utils.runtime.metadata.ModuleConstantMetadataSchema
import jp.co.soramitsu.fearless_utils.runtime.metadata.NMapSchema
import jp.co.soramitsu.fearless_utils.runtime.metadata.StorageEntryMetadataSchema
import jp.co.soramitsu.fearless_utils.runtime.metadata.StorageEntryModifier
import jp.co.soramitsu.fearless_utils.runtime.metadata.StorageHasher
import jp.co.soramitsu.fearless_utils.runtime.metadata.StorageMetadataSchema
import jp.co.soramitsu.fearless_utils.runtime.metadata.WithName
import jp.co.soramitsu.fearless_utils.runtime.metadata.groupByName
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.MapTypeV14
import jp.co.soramitsu.fearless_utils.scale.EncodableStruct
import java.math.BigInteger

class Module(
    override val name: String,
    val storage: Storage?,
    val calls: Map<String, MetadataFunction>?,
    val events: Map<String, Event>?,
    val constants: Map<String, Constant>,
    val errors: Map<String, Error>,
    val index: BigInteger
) : WithName

class Storage(
    val prefix: String,
    val entries: Map<String, StorageEntry>
) {
    constructor(
        typeRegistry: TypeRegistry,
        struct: EncodableStruct<StorageMetadataSchema>,
        moduleName: String
    ) : this(
        prefix = struct[StorageMetadataSchema.prefix],
        entries = struct[StorageMetadataSchema.entries].map {
            StorageEntry(
                typeRegistry,
                it,
                moduleName
            )
        }
            .groupByName()
    )

    operator fun get(entry: String) = entries[entry]
}

class StorageEntry(
    override val name: String,
    val modifier: StorageEntryModifier,
    val type: StorageEntryType,
    val default: ByteArray,
    val documentation: List<String>,
    val moduleName: String
) : WithName {

    constructor(
        typeRegistry: TypeRegistry,
        struct: EncodableStruct<StorageEntryMetadataSchema>,
        moduleName: String
    ) : this(
        name = struct[StorageEntryMetadataSchema.name],
        modifier = struct[StorageEntryMetadataSchema.modifier],
        type = StorageEntryType.from(typeRegistry, struct[StorageEntryMetadataSchema.type]),
        default = struct[StorageEntryMetadataSchema.default],
        documentation = struct[StorageEntryMetadataSchema.documentation],
        moduleName = moduleName
    )
}

sealed class StorageEntryType(
    val value: Type<*>?
) {

    companion object {
        fun fromV14(typeRegistry: TypeRegistry, value: Any?): StorageEntryType {
            val res = when (value) {
                is BigInteger -> {
                    Plain(typeRegistry, value.toString())
                }
                is EncodableStruct<*> -> {
                    when (value.schema) {
                        MapTypeV14 -> {
                            val hs = value[MapTypeV14.hashers]
                            val type = typeRegistry[value[MapTypeV14.key].toString()]
                                ?: cannotConstruct(value)
                            val keys = if (hs.size == 1) {
                                listOf(type)
                            } else {
                                if (type is Tuple) {
                                    type.typeReferences.mapNotNull { tr -> tr.value }
                                } else {
                                    cannotConstruct(value)
                                }
                            }
                            if (keys.size != hs.size) cannotConstruct(value)
                            MapV14(
                                hs,
                                keys,
                                typeRegistry[value[MapTypeV14.value].toString()]
                            )
                        }
                        else -> cannotConstruct(value)
                    }
                }
                else -> cannotConstruct(value)
            }
            return res
        }

        fun from(typeRegistry: TypeRegistry, value: Any?) = when (value) {
            is String -> Plain(typeRegistry, value)
            is EncodableStruct<*> -> when (value.schema) {
                MapSchema -> Map(typeRegistry, value)
                DoubleMapSchema -> DoubleMap(typeRegistry, value)
                NMapSchema -> NMap(typeRegistry, value)
                else -> cannotConstruct(value)
            }
            else -> cannotConstruct(value)
        }

        private fun cannotConstruct(from: Any?): Nothing {
            throw IllegalArgumentException("Cannot construct StorageEntryType from $from")
        }
    }

    class Plain(value: Type<*>?) : StorageEntryType(value) {

        constructor(
            typeRegistry: TypeRegistry,
            typeDef: String
        ) : this(
            typeRegistry[typeDef]
        )
    }

    class Map(
        val hasher: StorageHasher,
        val key: Type<*>?,
        value: Type<*>?,
        val unused: Boolean
    ) : StorageEntryType(value) {

        constructor(
            typeRegistry: TypeRegistry,
            struct: EncodableStruct<*>
        ) : this(
            hasher = struct[MapSchema.hasher],
            key = typeRegistry[struct[MapSchema.key]],
            value = typeRegistry[struct[MapSchema.value]],
            unused = struct[MapSchema.unused]
        )
    }

    class MapV14(
        val hashers: List<StorageHasher>,
        val key: List<Type<*>>,
        value: Type<*>?,
    ) : StorageEntryType(value)

    class DoubleMap(
        val key1Hasher: StorageHasher,
        val key1: Type<*>?,
        val key2: Type<*>?,
        value: Type<*>?,
        val key2Hasher: StorageHasher
    ) : StorageEntryType(value) {
        constructor(
            typeRegistry: TypeRegistry,
            struct: EncodableStruct<*>
        ) : this(
            key1Hasher = struct[DoubleMapSchema.key1Hasher],
            key2Hasher = struct[DoubleMapSchema.key2Hasher],
            key1 = typeRegistry[struct[DoubleMapSchema.key1]],
            key2 = typeRegistry[struct[DoubleMapSchema.key2]],
            value = typeRegistry[struct[DoubleMapSchema.value]]
        )
    }

    class NMap(
        val keys: List<Type<*>?>,
        val hashers: List<StorageHasher>,
        value: Type<*>?
    ) : StorageEntryType(value) {
        constructor(
            typeRegistry: TypeRegistry,
            struct: EncodableStruct<*>
        ) : this(
            keys = struct[NMapSchema.keys].map { typeRegistry[it] },
            hashers = struct[NMapSchema.hashers],
            value = typeRegistry[struct[NMapSchema.value]]
        )
    }
}

class MetadataFunction(
    override val name: String,
    val arguments: List<FunctionArgument>,
    val documentation: List<String>,
    val index: Pair<Int, Int>
) : WithName {
    constructor(
        typeRegistry: TypeRegistry,
        struct: EncodableStruct<FunctionMetadataSchema>,
        index: Pair<Int, Int>
    ) : this(
        name = struct[FunctionMetadataSchema.name],
        arguments = struct[FunctionMetadataSchema.arguments].map {
            FunctionArgument(typeRegistry, it)
        },
        documentation = struct[FunctionMetadataSchema.documentation],
        index = index
    )
}

class FunctionArgument(
    override val name: String,
    val type: Type<*>?
) : WithName {
    constructor(
        typeRegistry: TypeRegistry,
        struct: EncodableStruct<FunctionArgumentMetadataSchema>
    ) : this(
        name = struct[FunctionArgumentMetadataSchema.name],
        type = typeRegistry[struct[FunctionArgumentMetadataSchema.type]]
    )
}

class Event(
    override val name: String,
    val index: Pair<Int, Int>,
    val arguments: List<Type<*>?>,
    val documentation: List<String>
) : WithName {
    constructor(
        typeRegistry: TypeRegistry,
        struct: EncodableStruct<EventMetadataSchema>,
        index: Pair<Int, Int>
    ) : this(
        name = struct[EventMetadataSchema.name],
        arguments = struct[EventMetadataSchema.arguments].map { typeRegistry[it] },
        documentation = struct[EventMetadataSchema.documentation],
        index = index
    )
}

class Constant(
    override val name: String,
    val type: Type<*>?,
    val value: ByteArray,
    val documentation: List<String>
) : WithName {
    constructor(
        typeRegistry: TypeRegistry,
        struct: EncodableStruct<ModuleConstantMetadataSchema>
    ) : this(
        name = struct[ModuleConstantMetadataSchema.name],
        type = typeRegistry[struct[ModuleConstantMetadataSchema.type]],
        value = struct[ModuleConstantMetadataSchema.value],
        documentation = struct[ModuleConstantMetadataSchema.documentation]
    )
}

class Error(
    override val name: String,
    val documentation: List<String>
) : WithName {
    constructor(
        struct: EncodableStruct<ErrorMetadataSchema>
    ) : this(
        name = struct[ErrorMetadataSchema.name],
        documentation = struct[ErrorMetadataSchema.documentation]
    )
}
