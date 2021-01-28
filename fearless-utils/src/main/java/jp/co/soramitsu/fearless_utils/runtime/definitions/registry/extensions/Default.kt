package jp.co.soramitsu.fearless_utils.runtime.definitions.registry.extensions

import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypeConstructorExtension
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.splitTuple
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.TypeReference
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.FixedArray
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Option
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Tuple
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Vec
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.Compact
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.FixedByteArray
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.u8

object VectorExtension : WrapperExtension() {
    override val wrapperName = "Vec"

    override fun createWrapper(name: String, innerTypeRef: TypeReference) = Vec(name, innerTypeRef)
}

object CompactExtension : WrapperExtension() {
    override val wrapperName = "Compact"

    override fun createWrapper(name: String, innerTypeRef: TypeReference) = Compact(name)
}

object OptionExtension : WrapperExtension() {
    override val wrapperName = "Option"

    override fun createWrapper(name: String, innerTypeRef: TypeReference) = Option(name, innerTypeRef)
}

object TupleExtension : TypeConstructorExtension {

    override fun createType(name: String, typeDef: String, registry: TypeRegistry): Type<*>? {
        if (!typeDef.startsWith("(")) return null

        val innerTypeRefDefinitions = typeDef.splitTuple()

        val innerTypeRefs = innerTypeRefDefinitions.map(registry::getTypeReference)

        return Tuple(name, innerTypeRefs)
    }
}

object FixedArrayExtension : TypeConstructorExtension {

    override fun createType(name: String, typeDef: String, registry: TypeRegistry): Type<*>? {
        if (!typeDef.startsWith("[")) return null

        val withoutBrackets = typeDef.removeSurrounding("[", "]").replace(" ", "")
        val (typeName, lengthRaw) = withoutBrackets.split(";")

        val length = lengthRaw.toInt()

        val typeRef = registry.getTypeReference(typeName)

        return if (typeRef.value == u8) {
            FixedByteArray(name, length)
        } else {
            FixedArray(name, length, typeRef)
        }
    }
}