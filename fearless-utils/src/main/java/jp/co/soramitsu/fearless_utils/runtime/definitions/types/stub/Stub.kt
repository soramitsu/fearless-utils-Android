package jp.co.soramitsu.fearless_utils.runtime.definitions.types.stub

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.runtime.definitions.TypeRegistry

class StubNotResolvedException(val stubName: String) : Exception()

class Stub(name: String) : Type<Nothing>(name) {

    override fun replaceStubs(registry: TypeRegistry): Type<*> {
        return registry[name] ?: throw StubNotResolvedException(name)
    }

    override fun toString(): String {
        return "STUB($name)"
    }

    override fun decode(scaleCodecReader: ScaleCodecReader) =
        throw IllegalStateException("Cannot decode stub")

    override fun encode(scaleCodecWriter: ScaleCodecWriter, value: Nothing) =
        throw IllegalStateException("Cannot encode stub")

    override fun isValidInstance(instance: Any?): Boolean {
        return false
    }
}