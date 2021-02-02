package jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.fearless_utils.extensions.toHex
import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.Primitive
import jp.co.soramitsu.fearless_utils.scale.dataType.byte
import jp.co.soramitsu.fearless_utils.scale.dataType.uint16
import kotlin.math.max

sealed class Era {
    object Immortal : Era()

    class Mortal(val period: Int, val phase: Int) : Era()
}

object EraType : Primitive<Era>("Era") {

    override fun decode(scaleCodecReader: ScaleCodecReader, runtime: RuntimeSnapshot): Era {
        val firstByte = byte.read(scaleCodecReader).toHex()

        return if (firstByte == "00") {
            Era.Immortal
        } else {
            val secondByte = byte.read(scaleCodecReader).toHex()
            val encoded = (secondByte + firstByte).toInt(16)
            val period = 2 shl (encoded % 16)
            val quantizeFactor = max(1, period shr 12)
            val phase = (encoded shr 4) * quantizeFactor

            Era.Mortal(period, phase)
        }
    }

    override fun encode(scaleCodecWriter: ScaleCodecWriter, runtime: RuntimeSnapshot, value: Era) {
        when (value) {
            is Era.Immortal -> byte.write(scaleCodecWriter, 0)
            is Era.Mortal -> {
                val quantizeFactor = max(1, value.period shr 12)
                val trailingZeros = value.period.countTrailingZeroBits() - 1
                val encoded = trailingZeros.coerceIn(1, 15) or ((value.phase / quantizeFactor) shl 4)

                uint16.write(scaleCodecWriter, encoded)
            }
        }
    }

    override fun isValidInstance(instance: Any?) = instance is Era
}