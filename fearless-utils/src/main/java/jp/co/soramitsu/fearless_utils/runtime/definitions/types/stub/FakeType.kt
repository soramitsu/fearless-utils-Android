package jp.co.soramitsu.fearless_utils.runtime.definitions.types.stub

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.runtime.definitions.TypeRegistry

class FakeType(name: String) : Type<Nothing>(name) {

    override fun replaceStubs(registry: TypeRegistry): Type<*> {
        return this
    }

    override fun decode(scaleCodecReader: ScaleCodecReader): Nothing {
        throw IllegalArgumentException("Fake")
    }

    override fun encode(scaleCodecWriter: ScaleCodecWriter, value: Nothing) {
        throw IllegalArgumentException("Fake")
    }

    override fun isValidInstance(instance: Any?): Boolean {
        return false
    }
}