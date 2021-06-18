package jp.co.soramitsu.fearless_utils.runtime.metadata

import jp.co.soramitsu.fearless_utils.hash.Hasher.xxHash128
import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.schema.Context
import jp.co.soramitsu.schema.definitions.types.bytes
import jp.co.soramitsu.schema.extensions.toHexString

/**
 * @throws NoSuchElementException if module was not found
 */
fun RuntimeMetadata.module(index: Int): Module =
    modules.values.first { it.index == index.toBigInteger() }

fun RuntimeMetadata.moduleOrNull(index: Int): Module? = nullOnException { module(index) }

/**
 * @throws NoSuchElementException if module was not found
 */
fun RuntimeMetadata.module(name: String) = moduleOrNull(name) ?: throw NoSuchElementException()

fun RuntimeMetadata.moduleOrNull(name: String): Module? = modules[name]

/**
 * @throws NoSuchElementException if storage entry was not found
 */
fun Module.storage(name: String): StorageEntry =
    storageOrNull(name) ?: throw NoSuchElementException()

fun Module.storageOrNull(name: String): StorageEntry? = storage?.get(name)

/**
 * @throws NoSuchElementException if call was not found
 */
fun Module.call(index: Int): Function = requireElementInMap(calls, index)

fun Module.callOrNull(index: Int): Function? = nullOnException { call(index) }

/**
 * @throws NoSuchElementException if call was not found
 */
fun Module.call(name: String): Function = callOrNull(name) ?: throw NoSuchElementException()

fun Module.callOrNull(name: String): Function? = calls?.get(name)

/**
 * @throws NoSuchElementException if event was not found
 */
fun Module.event(index: Int): Event = requireElementInMap(events, index)

fun Module.eventOrNull(index: Int): Event? = nullOnException { event(index) }

/**
 * @throws NoSuchElementException if event was not found
 */
fun Module.event(name: String): Event = eventOrNull(name) ?: throw NoSuchElementException()

fun Module.eventOrNull(name: String): Event? = events?.get(name)

/**
 * Constructs a key for storage with no arguments.
 * This either fill be a full key for [StorageEntryType.Plain] entries,
 * or a prefix key for [StorageEntryType.Map] and [StorageEntryType.DoubleMap] entries
 *
 */
fun StorageEntry.storageKey(): String {
    return (moduleHash() + serviceHash()).toHexString(withPrefix = true)
}

fun StorageEntry.storageKeyOrNull() = nullOnException { storageKey() }

/**
 * Constructs a key for storage with one argument.
 * This either fill be a full key for [StorageEntryType.Map] entries,
 * or a prefix key for [StorageEntryType.DoubleMap] entries
 *
 * @throws IllegalArgumentException if storage entry has [StorageEntryType.Plain]  type
 * @throws IllegalStateException if some of types used for encoding cannot be resolved
 * @throws EncodeDecodeException if error happened during encoding
 */
fun StorageEntry.storageKey(runtime: RuntimeSnapshot, key: Any?): String {
    val (keyType, hasher) = when (type) {
        is StorageEntryType.Map -> type.key to type.hasher
        is StorageEntryType.DoubleMap -> type.key1 to type.key1Hasher
        else -> wrongEntryType()
    }

    val keyEncoded = keyType?.bytes(runtime as Context, key) ?: typeNotResolved(name)

    val storageKey = moduleHash() + serviceHash() + hasher.hashingFunction(keyEncoded)

    return storageKey.toHexString(withPrefix = true)
}

fun StorageEntry.storageKeyOrNull(runtime: RuntimeSnapshot, key1: Any?) = nullOnException {
    storageKey(runtime, key1)
}

/**
 **
 * Constructs a key for storage with two arguments.
 * This will be full key for [StorageEntryType.DoubleMap] entries,
 *
 * @throws IllegalArgumentException if storage entry has [StorageEntryType.Plain] or [StorageEntryType.Map] type
 * @throws IllegalStateException if some of types used for encoding cannot be resolved
 * @throws EncodeDecodeException if error happened during encoding
 */
fun StorageEntry.storageKey(runtime: RuntimeSnapshot, key1: Any?, key2: Any?): String {
    if (type !is StorageEntryType.DoubleMap) wrongEntryType()

    val key1Type = type.key1 ?: typeNotResolved(name)
    val key2Type = type.key2 ?: typeNotResolved(name)

    val key1Encoded = key1Type.bytes(runtime as Context, key1)
    val key2Encoded = key2Type.bytes(runtime as Context, key2)

    val key1Hashed = type.key1Hasher.hashingFunction(key1Encoded)
    val key2Hashed = type.key2Hasher.hashingFunction(key2Encoded)

    val storageKey = moduleHash() + serviceHash() + key1Hashed + key2Hashed

    return storageKey.toHexString(withPrefix = true)
}

fun StorageEntry.storageKeyOrNull(runtime: RuntimeSnapshot, key1: Any?, key2: Any?) =
    nullOnException {
        storageKey(runtime, key1, key2)
    }

private fun typeNotResolved(entryName: String): Nothing =
    throw IllegalStateException("Cannot resolve key or value type for storage entry `$entryName`")

private fun wrongEntryType(): Nothing =
    throw IllegalArgumentException("Storage entry has different type than requested for storage key")

private fun StorageEntry.moduleHash() = moduleName.toByteArray().xxHash128()

private fun StorageEntry.serviceHash() = name.toByteArray().xxHash128()

private inline fun <T> nullOnException(block: () -> T): T? {
    return runCatching(block).getOrNull()
}

private fun <V> requireElementInMap(map: Map<String, V>?, index: Int): V {
    if (map == null) throw NoSuchElementException()

    return map.values.elementAtOrNull(index) ?: throw NoSuchElementException()
}
