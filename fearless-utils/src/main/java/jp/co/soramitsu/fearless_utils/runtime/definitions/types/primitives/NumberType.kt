package jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives

import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import java.math.BigInteger

abstract class NumberType(name: String) : Primitive<BigInteger>(name) {

    override fun encode(scaleCodecWriter: ScaleCodecWriter, value: BigInteger) {
        encodeNumber(scaleCodecWriter, value)
    }

    abstract fun encodeNumber(writer: ScaleCodecWriter, number: BigInteger)

    override fun isValidInstance(instance: Any?): Boolean {
        return instance is BigInteger
    }
}