package jp.co.soramitsu.fearless_utils.runtime.definitions.types.stub

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.fearless_utils.runtime.definitions.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import java.lang.IllegalArgumentException

class TypeAlias(val alias: String, val original: String) : Type<Nothing>(alias) {
    override fun replaceStubs(registry: TypeRegistry): Type<*> = this

    override fun decode(scaleCodecReader: ScaleCodecReader): Nothing {
        throw IllegalArgumentException("TypeAlias")
    }

    override fun encode(scaleCodecWriter: ScaleCodecWriter, value: Nothing) {
        throw IllegalArgumentException("TypeAlias")
    }

    override fun isValidInstance(instance: Any?): Boolean {
        return true
    }
}