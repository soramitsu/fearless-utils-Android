package jp.co.soramitsu.fearless_utils.runtime.metadata

import jp.co.soramitsu.fearless_utils.extensions.toHexString
import jp.co.soramitsu.fearless_utils.hash.Hasher.xxHash128
import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.bytesOrNull
import jp.co.soramitsu.fearless_utils.scale.EncodableStruct
import java.math.BigInteger

interface WithName {
    val name: String
}

fun <T : WithName> List<T>.groupByName() = map { it.name to it }.toMap()

class RuntimeMetadata(
    val runtimeVersion: BigInteger,
    val modules: Map<String, Module>,
    val extrinsic: ExtrinsicMetadata
) {
    constructor(
        typeRegistry: TypeRegistry,
        struct: EncodableStruct<RuntimeMetadataSchema>
    ) : this(
        runtimeVersion = struct[RuntimeMetadataSchema.runtimeVersion].toInt().toBigInteger(),
        modules = struct[RuntimeMetadataSchema.modules].map { Module(typeRegistry, it) }
            .groupByName(),
        extrinsic = ExtrinsicMetadata(struct[RuntimeMetadataSchema.extrinsic])
    )

    fun getModule(index: Int): Module? = modules.values.find { it.index == index.toBigInteger() }

    fun getCall(moduleIndex: Int, callIndex: Int): Function? {
        val module = getModule(moduleIndex)

        return module?.calls?.values?.elementAtOrNull(callIndex)
    }

    fun getEvent(moduleIndex: Int, eventIndex: Int): Event? {
        val module = getModule(moduleIndex)

        return module?.events?.values?.elementAtOrNull(eventIndex)
    }
}

class Module(
    override val name: String,
    val storage: Storage?,
    val calls: Map<String, Function>?,
    val events: Map<String, Event>?,
    val constants: Map<String, Constant>,
    val errors: Map<String, Error>,
    val index: BigInteger
) : WithName {

    constructor(
        typeRegistry: TypeRegistry,
        struct: EncodableStruct<ModuleMetadataSchema>
    ) : this(
        name = struct.name,
        storage = struct[ModuleMetadataSchema.storage]?.let {
            Storage(
                typeRegistry,
                it,
                struct.name
            )
        },
        calls = struct[ModuleMetadataSchema.calls]?.map { Function(typeRegistry, it) }
            ?.groupByName(),
        events = struct[ModuleMetadataSchema.events]?.map { Event(typeRegistry, it) }
            ?.groupByName(),
        constants = struct[ModuleMetadataSchema.constants].map { Constant(typeRegistry, it) }
            .groupByName(),
        errors = struct[ModuleMetadataSchema.errors].map(::Error).groupByName(),
        index = struct[ModuleMetadataSchema.index].toInt().toBigInteger()
    )
}

private val EncodableStruct<ModuleMetadataSchema>.name: String
    get() = get(ModuleMetadataSchema.name)

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
    private val moduleName: String
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

    fun storageKey(): String? {
        if (type !is StorageEntryType.Plain) return null

        return (moduleHash() + serviceHash()).toHexString(withPrefix = true)
    }

    fun storageKey(runtime: RuntimeSnapshot, key1: Any?): String? {
        if (type !is StorageEntryType.Map) return null

        val key1Encoded = type.key?.bytesOrNull(runtime, key1) ?: return null

        val storageKey = moduleHash() + serviceHash() + type.hasher.hashingFunction(key1Encoded)

        return storageKey.toHexString(withPrefix = true)
    }

    fun storageKey(runtime: RuntimeSnapshot, key1: Any?, key2: Any?): String? {
        if (type !is StorageEntryType.DoubleMap) return null

        val key1Encoded = type.key1?.bytesOrNull(runtime, key1) ?: return null
        val key2Encoded = type.key2?.bytesOrNull(runtime, key2) ?: return null

        val key1Hashed = type.key1Hasher.hashingFunction(key1Encoded)
        val key2Hashed = type.key2Hasher.hashingFunction(key2Encoded)

        val storageKey = moduleHash() + serviceHash() + key1Hashed + key2Hashed

        return storageKey.toHexString(withPrefix = true)
    }

    private fun moduleHash() = moduleName.toByteArray().xxHash128()

    private fun serviceHash() = name.toByteArray().xxHash128()
}

sealed class StorageEntryType {
    companion object {
        fun from(typeRegistry: TypeRegistry, value: Any?) = when (value) {
            is String -> Plain(typeRegistry, value)
            is EncodableStruct<*> -> when (value.schema) {
                MapSchema -> Map(typeRegistry, value)
                DoubleMapSchema -> DoubleMap(typeRegistry, value)
                else -> cannotConstruct(value)
            }
            else -> cannotConstruct(value)
        }

        private fun cannotConstruct(from: Any?): Nothing {
            throw IllegalArgumentException("Cannot construct StorageEntryType from $from")
        }
    }

    class Plain(val value: Type<*>?) : StorageEntryType() {

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
        val value: Type<*>?,
        val unused: Boolean
    ) : StorageEntryType() {

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

    class DoubleMap(
        val key1Hasher: StorageHasher,
        val key1: Type<*>?,
        val key2: Type<*>?,
        val value: Type<*>?,
        val key2Hasher: StorageHasher
    ) : StorageEntryType() {
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
}

class Function(
    override val name: String,
    val arguments: List<FunctionArgument>,
    val documentation: List<String>
) : WithName {
    constructor(
        typeRegistry: TypeRegistry,
        struct: EncodableStruct<FunctionMetadataSchema>
    ) : this(
        name = struct[FunctionMetadataSchema.name],
        arguments = struct[FunctionMetadataSchema.arguments].map {
            FunctionArgument(
                typeRegistry,
                it
            )
        },
        documentation = struct[FunctionMetadataSchema.documentation]
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
    val arguments: List<Type<*>?>,
    val documentation: List<String>
) : WithName {
    constructor(
        typeRegistry: TypeRegistry,
        struct: EncodableStruct<EventMetadataSchema>
    ) : this(
        name = struct[EventMetadataSchema.name],
        arguments = struct[EventMetadataSchema.arguments].map { typeRegistry[it] },
        documentation = struct[EventMetadataSchema.documentation]
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

class ExtrinsicMetadata(
    val version: BigInteger,
    val signedExtensions: List<String>
) {
    constructor(
        struct: EncodableStruct<ExtrinsicMetadataSchema>
    ) : this(
        version = struct[ExtrinsicMetadataSchema.version].toInt().toBigInteger(),
        signedExtensions = struct[ExtrinsicMetadataSchema.signedExtensions]
    )
}