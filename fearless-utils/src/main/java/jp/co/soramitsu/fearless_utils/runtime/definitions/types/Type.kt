package jp.co.soramitsu.fearless_utils.runtime.definitions.types

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.fearless_utils.extensions.fromHex
import jp.co.soramitsu.fearless_utils.extensions.toHexString
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Alias
import java.io.ByteArrayOutputStream

class InvalidInstanceException : Exception()

typealias TypeMapping = LinkedHashMap<String, TypeReference>

class TypeReference(var value: Type<*>?) {
    private var resolutionInProgress: Boolean = false

    fun requireValue() = value ?: throw IllegalArgumentException("TypeReference is null")

    fun isResolved(): Boolean {
        if (isInRecursion()) {
            return true
        }

        resolutionInProgress = true

        val resolutionResult = resolveRecursive()

        resolutionInProgress = false

        return resolutionResult
    }

    private fun resolveRecursive() = value?.isFullyResolved ?: false

    private fun isInRecursion() = resolutionInProgress
}

fun TypeReference.resolveAliasing(): TypeReference {
    var aliased = this

    while (true) {
        val aliasedValue = aliased.value

        if (aliasedValue !is Alias) break

        aliased = aliasedValue.aliasedReference
    }

    return aliased
}

fun Type<*>.resolveAliasing(): Type<*>? {
    if (this !is Alias) return this

    return aliasedReference.resolveAliasing().value
}

fun Type<*>?.isFullyResolved() = this?.isFullyResolved ?: false

abstract class Type<InstanceType>(val name: String) {

    abstract val isFullyResolved: Boolean

    abstract fun decode(scaleCodecReader: ScaleCodecReader): InstanceType

    abstract fun encode(scaleCodecWriter: ScaleCodecWriter, value: InstanceType)

    @Suppress("UNCHECKED_CAST")
    fun encodeUnsafe(scaleCodecWriter: ScaleCodecWriter, value: Any?) {
        if (!isValidInstance(value)) throw InvalidInstanceException()

        encode(scaleCodecWriter, value as InstanceType)
    }

    abstract fun isValidInstance(instance: Any?): Boolean

    override fun toString(): String {
        return name
    }
}

fun <I> Type<I>.fromByteArray(byteArray: ByteArray): I {
    val reader = ScaleCodecReader(byteArray)

    return decode(reader)
}

fun <I> Type<I>.fromHex(hex: String): I {
    return fromByteArray(hex.fromHex())
}

fun <I, T : Type<I>> T.toByteArray(value: I): ByteArray {
    val stream = ByteArrayOutputStream()
    val writer = ScaleCodecWriter(stream)

    encode(writer, value)

    return stream.toByteArray()
}

fun <I, T : Type<I>> T.toHex(value: I) = toByteArray(value).toHexString(withPrefix = true)