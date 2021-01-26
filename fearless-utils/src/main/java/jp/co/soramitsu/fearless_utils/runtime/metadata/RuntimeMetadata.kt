package jp.co.soramitsu.fearless_utils.runtime.metadata

import jp.co.soramitsu.fearless_utils.scale.EncodableStruct
import jp.co.soramitsu.fearless_utils.scale.Schema
import jp.co.soramitsu.fearless_utils.scale.bool
import jp.co.soramitsu.fearless_utils.scale.byteArray
import jp.co.soramitsu.fearless_utils.scale.dataType.scalable
import jp.co.soramitsu.fearless_utils.scale.enum
import jp.co.soramitsu.fearless_utils.scale.schema
import jp.co.soramitsu.fearless_utils.scale.string
import jp.co.soramitsu.fearless_utils.scale.uint32
import jp.co.soramitsu.fearless_utils.scale.uint8
import jp.co.soramitsu.fearless_utils.scale.vector
import jp.co.soramitsu.fearless_utils.scale.dataType.string as stringType


object RuntimeMetadata : Schema<RuntimeMetadata>() {
    val magicNumber by uint32()

    val runtimeVersion by uint8()

    val modules by vector(ModuleMetadata)

    val extrinsic by schema(ExtrinsicMetadata)
}

object ModuleMetadata : Schema<ModuleMetadata>() {
    val name by string()

    val storage by schema(StorageMetadata).optional()

    val calls by vector(FunctionMetadata).optional()

    val events by vector(EventMetadata).optional()

    val constants by vector(ModuleConstantMetadata)

    val errors by vector(ErrorMetadata)

    val index by uint8()
}

object StorageMetadata : Schema<StorageMetadata>() {
    val prefix by string()

    val entries by vector(StorageEntryMetadata)
}

object StorageEntryMetadata : Schema<StorageEntryMetadata>() {
    val name by string()

    val modifier by enum(StorageEntryModifier::class)

    val type by enum(
        stringType, // plain
        scalable(Map),
        scalable(DoubleMap)
    )

    val default by byteArray() // vector<u8>

    val documentation by vector(stringType)
}

enum class StorageEntryModifier {
    Optional, Default
}

object Map : Schema<Map>() {
    val hasher by enum(StorageHasher::class)
    val key by string()
    val value by string()
    val unused by bool()
}

object DoubleMap : Schema<DoubleMap>() {
    val key1Hasher by enum(StorageHasher::class)
    val key1 by string()
    val key2 by string()
    val value by string()
    val key2Hasher by enum(StorageHasher::class)
}

enum class StorageHasher {
    Blake2_128,
    Blake2_256,
    Blake2_128Concat,
    Twox128,
    Twox256,
    Twox64Concat,
    Identity
}

object FunctionMetadata : Schema<FunctionMetadata>() {
    val name by string()

    val arguments by vector(FunctionArgumentMetadata)

    val documentation by vector(stringType)
}

object FunctionArgumentMetadata : Schema<FunctionArgumentMetadata>() {
    val name by string()

    val type by string()
}

object EventMetadata : Schema<EventMetadata>() {
    val name by string()

    val arguments by vector(stringType)

    val documentation by vector(stringType)
}

object ModuleConstantMetadata : Schema<ModuleConstantMetadata>() {
    val name by string()

    val type by string()

    val value by byteArray() // vector<u8>

    val documentation by vector(stringType)
}

object ErrorMetadata : Schema<ErrorMetadata>() {
    val name by string()

    val documentation by vector(stringType)
}

object ExtrinsicMetadata : Schema<ExtrinsicMetadata>() {
    val version by uint8()

    val signed_extensions by vector(stringType)
}

fun EncodableStruct<RuntimeMetadata>.module(name: String) = get(RuntimeMetadata.modules).find { it[ModuleMetadata.name] == name }

fun EncodableStruct<ModuleMetadata>.call(name: String) = get(ModuleMetadata.calls)?.find { it[FunctionMetadata.name] == name }

fun EncodableStruct<ModuleMetadata>.storage(name: String) = get(ModuleMetadata.storage)?.get(StorageMetadata.entries)?.find { it[StorageEntryMetadata.name] == name }