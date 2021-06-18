package jp.co.soramitsu.schema.definitions.types.stub

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.schema.definitions.types.Type
import jp.co.soramitsu.schema.Context

class FakeType(name: String) : Type<Nothing>(name) {

    override fun decode(scaleCodecReader: ScaleCodecReader, context: Context): Nothing {
        throw IllegalArgumentException("Fake")
    }

    override fun encode(scaleCodecWriter: ScaleCodecWriter, runtime: Context, value: Nothing) {
        throw IllegalArgumentException("Fake")
    }

    override fun isValidInstance(instance: Any?): Boolean {
        return false
    }

    override val isFullyResolved = true
}
