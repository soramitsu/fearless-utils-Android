package jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.fearless_utils.scale.dataType.uint
import java.math.BigInteger

val i128 = IntType(128)

class IntType(bits: Int) : NumberType("i$bits") {

    init {
        require(bits % 8 == 0)
    }

    val bytes = bits / 8

    private val codec = uint(size = bytes)

    override fun encode(
        scaleCodecWriter: ScaleCodecWriter,
        runtime: RuntimeSnapshot,
        value: BigInteger
    ) {
        codec.write(scaleCodecWriter, value)
    }

    override fun decode(scaleCodecReader: ScaleCodecReader, runtime: RuntimeSnapshot) =
        codec.read(scaleCodecReader)
}