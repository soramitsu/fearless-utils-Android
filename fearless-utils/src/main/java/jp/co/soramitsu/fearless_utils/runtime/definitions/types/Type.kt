package jp.co.soramitsu.fearless_utils.runtime.definitions.types

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.fearless_utils.extensions.fromHex
import jp.co.soramitsu.fearless_utils.extensions.toHexString
import jp.co.soramitsu.fearless_utils.runtime.definitions.TypeRegistry
import java.io.ByteArrayOutputStream

class InvalidInstanceException : Exception()

typealias TypeMapping = LinkedHashMap<String, Type<*>>

abstract class Type<InstanceType>(val name: String) {

    /**
     * Contract - if nothing was replaced, the return type should be === current
     */
    internal abstract fun replaceStubs(registry: TypeRegistry): Type<*>

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


fun <I, T: Type<I>> T.fromByteArray(byteArray: ByteArray): I {
    val reader = ScaleCodecReader(byteArray)

    return decode(reader)
}

fun <I, T: Type<I>> T.fromHex(hex: String): I {
   return fromByteArray(hex.fromHex())
}

fun <I, T: Type<I>> T.toByteArray(value: I) : ByteArray {
    val stream = ByteArrayOutputStream()
    val writer = ScaleCodecWriter(stream)

    encode(writer, value)

    return stream.toByteArray()
}

fun <I, T: Type<I>> T.toHex(value: I) = toByteArray(value).toHexString(withPrefix = true)