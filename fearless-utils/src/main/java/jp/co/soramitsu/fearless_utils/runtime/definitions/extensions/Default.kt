package jp.co.soramitsu.fearless_utils.runtime.definitions.extensions

import jp.co.soramitsu.fearless_utils.runtime.definitions.TypeConstructorExtension
import jp.co.soramitsu.fearless_utils.runtime.definitions.splitTuple
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.FixedArray
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Option
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Tuple
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Vec
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.Compact
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.FixedByteArray
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.NumberType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.u8

object VectorExtension : WrapperExtension() {
    override val wrapperName = "Vec"

    override fun createWrapper(name: String, innerType: Type<*>) = Vec(name, innerType)
}

object CompactExtension : WrapperExtension() {
    override val wrapperName = "Compact"

    override fun createWrapper(name: String, innerType: Type<*>): Type<*>? {
        if (innerType !is NumberType) return null

        return Compact(name, innerType)
    }
}

object OptionExtension : WrapperExtension() {
    override val wrapperName = "Option"

    override fun createWrapper(name: String, innerType: Type<*>) = Option(name, innerType)
}

object TupleExtension : TypeConstructorExtension {

    override fun createType(typeDef: String, typeResolver: (String) -> Type<*>?): Type<*>? {
        if (!typeDef.startsWith("(")) return null

        val innerTypeDefinitions = typeDef.splitTuple()

        val innerTypes = innerTypeDefinitions.map {
            val result = typeResolver(it)

            result ?: return null
        }

        return Tuple(typeDef, innerTypes)
    }
}

object FixedArrayExtension : TypeConstructorExtension {

    override fun createType(typeDef: String, typeResolver: (String) -> Type<*>?): Type<*>? {
        if (!typeDef.startsWith("[")) return null

        val withoutBrackets = typeDef.removeSurrounding("[", "]").replace(" ", "")
        val (typeName, lengthRaw) = withoutBrackets.split(";")

        val length = lengthRaw.toInt()

        val type = typeResolver(typeName) ?: return null

        return if (type == u8) {
            FixedByteArray(typeDef, length)
        } else {
            FixedArray(typeDef, length, type)
        }
    }
}