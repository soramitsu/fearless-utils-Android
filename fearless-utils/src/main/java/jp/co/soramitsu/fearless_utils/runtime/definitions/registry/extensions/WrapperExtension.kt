package jp.co.soramitsu.fearless_utils.runtime.definitions.registry.extensions

import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypeConstructorExtension
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.TypeReference

abstract class WrapperExtension : TypeConstructorExtension {

    abstract val wrapperName: String

    abstract fun createWrapper(name: String, innerTypeRef: TypeReference): Type<*>?

    override fun createType(name: String, typeDef: String, registry: TypeRegistry): Type<*>? {
        if (!typeDef.startsWith("$wrapperName<")) return null

        val innerTypeDef = typeDef.removeSurrounding("$wrapperName<", ">")

        val innerTypeRef = registry.getTypeReference(innerTypeDef)

        return createWrapper(name, innerTypeRef)
    }
}