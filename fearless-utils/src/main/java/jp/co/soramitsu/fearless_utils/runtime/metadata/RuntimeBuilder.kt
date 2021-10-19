package jp.co.soramitsu.fearless_utils.runtime.metadata

import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.DictEnum
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.Constant
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.Error
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.Event
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.FunctionArgument
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.MetadataFunction
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.Module
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.Storage
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.StorageEntry
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.StorageEntryType
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.ExtrinsicMetadataV14
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.PalletCallMetadataV14
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.PalletConstantMetadataV14
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.PalletErrorMetadataV14
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.PalletEventMetadataV14
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.PalletMetadataV14
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.RuntimeMetadataSchemaV14
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.SignedExtensionMetadataV14
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.StorageEntryMetadataV14
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.StorageMetadataV14
import jp.co.soramitsu.fearless_utils.scale.EncodableStruct
import java.math.BigInteger

object RuntimeBuilder {
    fun buildExtrinsic(reader: RuntimeMetadataReader): ExtrinsicMetadata {
        val version: BigInteger
        val ext: List<String>
        val v = reader.getMagic()[Magic.runtimeVersion].toInt()
        when {
            v < 14 -> {
                val schema = reader.getSchema()[RuntimeMetadataSchema.extrinsic]
                version = schema[ExtrinsicMetadataSchema.version].toInt().toBigInteger()
                ext = schema[ExtrinsicMetadataSchema.signedExtensions]
            }
            else -> {
                val schema = reader.getSchema()[RuntimeMetadataSchemaV14.extrinsic]
                version = schema[ExtrinsicMetadataV14.version].toInt().toBigInteger()
                ext = schema[ExtrinsicMetadataV14.signedExtensions].map {
                    it[SignedExtensionMetadataV14.identifier]
                }
            }
        }
        return ExtrinsicMetadata(version, ext)
    }

    fun buildModules(
        reader: RuntimeMetadataReader,
        typeRegistry: TypeRegistry
    ): Map<String, Module> {
        val v = reader.getMagic()[Magic.runtimeVersion].toInt()
        val res = when {
            v < 14 -> {
                reader.getSchema()[RuntimeMetadataSchema.modules].map {
                    buildModule(typeRegistry, v, it)
                }.groupByName()
            }
            else -> {
                reader.getSchema()[RuntimeMetadataSchemaV14.pallets].map {
                    buildModule(typeRegistry, v, it)
                }.groupByName()
            }
        }
        return res
    }

    private fun buildModule(
        typeRegistry: TypeRegistry,
        v: Int,
        struct: EncodableStruct<*>,
    ): Module {
        val name: String
        val storage: Storage?
        val calls: Map<String, MetadataFunction>?
        val events: Map<String, Event>?
        val constants: Map<String, Constant>
        val errors: Map<String, Error>
        val index: Int
        when {
            v < 14 -> {
                name = struct[ModuleMetadataSchema.name]
                index = struct[ModuleMetadataSchema.index].toInt()
                storage =
                    struct[ModuleMetadataSchema.storage]?.let {
                        buildStorage(
                            typeRegistry,
                            v,
                            it,
                            name
                        )
                    }
                calls = struct[ModuleMetadataSchema.calls]?.let {
                    buildCalls(
                        typeRegistry,
                        v,
                        it,
                        index
                    )
                }
                events =
                    struct[ModuleMetadataSchema.events]?.let {
                        buildEvents(
                            typeRegistry,
                            v,
                            it,
                            index
                        )
                    }
                constants = buildConstants(typeRegistry, v, struct[ModuleMetadataSchema.constants])
                errors = buildErrors(typeRegistry, v, struct[ModuleMetadataSchema.errors])
            }
            else -> {
                name = struct[PalletMetadataV14.name]
                index = struct[PalletMetadataV14.index].toInt()
                storage =
                    struct[PalletMetadataV14.storage]?.let {
                        buildStorage(
                            typeRegistry,
                            v,
                            it,
                            name
                        )
                    }
                calls =
                    struct[PalletMetadataV14.calls]?.let { buildCalls(typeRegistry, v, it, index) }
                events = struct[PalletMetadataV14.events]?.let {
                    buildEvents(
                        typeRegistry,
                        v,
                        it,
                        index
                    )
                }
                constants = buildConstants(typeRegistry, v, struct[PalletMetadataV14.constants])
                errors = struct[PalletMetadataV14.errors]?.let {
                    buildErrors(typeRegistry, v, it)
                } ?: emptyMap()
            }
        }
        return Module(
            name = name,
            storage = storage,
            calls = calls,
            events = events,
            constants = constants,
            errors = errors,
            index = index.toBigInteger(),
        )
    }

    private fun buildStorage(
        typeRegistry: TypeRegistry,
        v: Int,
        struct: EncodableStruct<*>,
        name: String,
    ): Storage {
        val res = when {
            v < 14 -> {
                val s = struct as EncodableStruct<StorageMetadataSchema>
                Storage(typeRegistry, s, name)
            }
            else -> {
                val s = struct as EncodableStruct<StorageMetadataV14>
                val prefix = s[StorageMetadataV14.prefix]
                val entries = s[StorageMetadataV14.entries].map {
                    val sName = it[StorageEntryMetadataV14.name]
                    val m = it[StorageEntryMetadataV14.modifier]
                    val type =
                        StorageEntryType.fromV14(typeRegistry, it[StorageEntryMetadataV14.type])
                    val def = it[StorageEntryMetadataV14.default]
                    val docs = it[StorageEntryMetadataV14.documentation]
                    StorageEntry(sName, m, type, def, docs, name)
                }.groupByName()
                Storage(prefix, entries)
            }
        }
        return res
    }

    private fun buildCalls(
        typeRegistry: TypeRegistry,
        v: Int,
        struct: Any,
        indexModule: Int,
    ): Map<String, MetadataFunction> {
        val res = when {
            v < 14 -> {
                val s = struct as List<EncodableStruct<FunctionMetadataSchema>>
                s.mapIndexed { index, encodableStruct ->
                    MetadataFunction(typeRegistry, encodableStruct, indexModule to index)
                }.groupByName()
            }
            else -> {
                val s = struct as EncodableStruct<PalletCallMetadataV14>
                val type = typeRegistry[s[PalletCallMetadataV14.type].toString()]
                if (type is DictEnum) {
                    val elements = type.elements.map {
                        val tr = it.value.value as Struct
                        val args = tr.mapping.map { mapEntry ->
                            FunctionArgument(mapEntry.key, mapEntry.value.value)
                        }
                        MetadataFunction(it.name, args, emptyList(), indexModule to tr.name.toInt())
                    }.groupByName()
                    elements
                } else {
                    emptyMap()
                }
            }
        }
        return res
    }

    private fun buildEvents(
        typeRegistry: TypeRegistry,
        v: Int,
        struct: Any,
        indexModule: Int,
    ): Map<String, Event> {
        val res = when {
            v < 14 -> {
                val s = struct as List<EncodableStruct<EventMetadataSchema>>
                s.mapIndexed { index, encodableStruct ->
                    Event(typeRegistry, encodableStruct, indexModule to index)
                }.groupByName()
            }
            else -> {
                val s = struct as EncodableStruct<PalletEventMetadataV14>
                val type = typeRegistry[s[PalletEventMetadataV14.type].toString()]
                if (type is DictEnum) {
                    val elements = type.elements.map {
                        val tr = it.value.value as Struct
                        val args = tr.mapping.map { mapEntry ->
                            mapEntry.value.value
                        }
                        Event(it.name, indexModule to tr.name.toInt(), args, emptyList())
                    }.groupByName()
                    elements
                } else {
                    emptyMap()
                }
            }
        }
        return res
    }

    private fun buildConstants(
        typeRegistry: TypeRegistry,
        v: Int,
        struct: List<EncodableStruct<*>>,
    ): Map<String, Constant> {
        val res = when {
            v < 14 -> {
                val s = struct as List<EncodableStruct<ModuleConstantMetadataSchema>>
                s.map { Constant(typeRegistry, it) }.groupByName()
            }
            else -> {
                val s = struct as List<EncodableStruct<PalletConstantMetadataV14>>
                val constants = s.map {
                    Constant(
                        it[PalletConstantMetadataV14.name],
                        typeRegistry[it[PalletConstantMetadataV14.type].toString()],
                        it[PalletConstantMetadataV14.value],
                        it[PalletConstantMetadataV14.documentation],
                    )
                }.groupByName()
                constants
            }
        }
        return res
    }

    private fun buildErrors(
        typeRegistry: TypeRegistry,
        v: Int,
        struct: Any,
    ): Map<String, Error> {
        val res = when {
            v < 14 -> {
                val s = struct as List<EncodableStruct<ErrorMetadataSchema>>
                s.map { Error(it) }.groupByName()
            }
            else -> {
                val s = struct as EncodableStruct<PalletErrorMetadataV14>
                val type = typeRegistry[s[PalletErrorMetadataV14.type].toString()]
                if (type is DictEnum) {
                    val elements = type.elements.map {
                        Error(it.name, emptyList())
                    }.groupByName()
                    elements
                } else {
                    emptyMap()
                }
            }
        }
        return res
    }
}
