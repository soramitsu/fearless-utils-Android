package jp.co.soramitsu.schema.definitions.types.primitives

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.schema.scale.dataType.compactInt
import java.math.BigInteger
import jp.co.soramitsu.schema.Context

class Compact(name: String) : NumberType(name) {

    override fun decode(scaleCodecReader: ScaleCodecReader, context: Context): BigInteger {
        return compactInt.read(scaleCodecReader)
    }

    override fun encode(scaleCodecWriter: ScaleCodecWriter, runtime: Context, value: BigInteger) {
        return compactInt.write(scaleCodecWriter, value)
    }
}
