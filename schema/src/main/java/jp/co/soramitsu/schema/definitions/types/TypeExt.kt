package jp.co.soramitsu.schema.definitions.types

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.schema.extensions.ensureExceptionType
import jp.co.soramitsu.schema.extensions.fromHex
import jp.co.soramitsu.schema.extensions.toHexString
import jp.co.soramitsu.schema.Context
import jp.co.soramitsu.schema.definitions.types.composite.Alias
import jp.co.soramitsu.schema.definitions.types.errors.EncodeDecodeException
import java.io.ByteArrayOutputStream

/**
 * @throws CyclicAliasingException
 */
fun Type<*>.skipAliases(): Type<*>? {
    if (this !is Alias) return this

    return aliasedReference.skipAliasesOrNull()?.value
}

fun Type<*>?.isFullyResolved() = this?.isFullyResolved ?: false

/**
 * @throws EncodeDecodeException
 */
fun <I> Type<I>.fromByteArray(runtime: Context, byteArray: ByteArray): I {
    val reader = ScaleCodecReader(byteArray)

    return ensureUnifiedException { decode(reader, runtime) }
}

/**
 * @throws EncodeDecodeException
 */
fun <I> Type<I>.fromHex(runtime: Context, hex: String): I {
    return ensureUnifiedException { fromByteArray(runtime, hex.fromHex()) }
}

fun <I> Type<I>.fromByteArrayOrNull(runtime: Context, byteArray: ByteArray): I? {
    return runCatching { fromByteArray(runtime, byteArray) }.getOrNull()
}

fun <I> Type<I>.fromHexOrNull(runtime: Context, hex: String): I? {
    return runCatching { fromHex(runtime, hex) }.getOrNull()
}

/**
 * @throws EncodeDecodeException
 */
fun <I> Type<I>.toByteArray(runtime: Context, value: I): ByteArray {
    return ensureUnifiedException {
        useScaleWriter { encode(this, runtime, value) }
    }
}

/**
 * Type-unsafe version of [toByteArray]
 *
 * @throws EncodeDecodeException
 */
fun Type<*>.bytes(runtime: Context, value: Any?): ByteArray {
    return ensureUnifiedException {
        useScaleWriter { encodeUnsafe(this, runtime, value) }
    }
}

fun <I> Type<I>.toByteArrayOrNull(runtime: Context, value: I): ByteArray? {
    return runCatching { toByteArray(runtime, value) }.getOrNull()
}

fun Type<*>.bytesOrNull(runtime: Context, value: Any?): ByteArray? {
    return runCatching { bytes(runtime, value) }.getOrNull()
}

/**
 * @throws EncodeDecodeException
 */
fun <I> Type<I>.toHex(runtime: Context, value: I) =
    toByteArray(runtime, value).toHexString(withPrefix = true)

fun <I> Type<I>.toHexOrNull(runtime: Context, value: I) =
    toByteArrayOrNull(runtime, value)?.toHexString(withPrefix = true)

fun useScaleWriter(use: ScaleCodecWriter.() -> Unit): ByteArray {
    val stream = ByteArrayOutputStream()
    val writer = ScaleCodecWriter(stream)

    writer.use()

    return stream.toByteArray()
}

private inline fun <R> ensureUnifiedException(block: () -> R): R {
    return ensureExceptionType(::EncodeDecodeException, block)
}
