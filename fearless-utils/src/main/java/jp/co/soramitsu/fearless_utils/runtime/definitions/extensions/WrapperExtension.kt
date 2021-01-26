package jp.co.soramitsu.fearless_utils.runtime.definitions.extensions

import jp.co.soramitsu.fearless_utils.runtime.definitions.TypeConstructorExtension
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type

abstract class WrapperExtension : TypeConstructorExtension {

    abstract val wrapperName: String

    abstract fun createWrapper(name: String, innerType: Type<*>): Type<*>?

    override fun createType(typeDef: String, typeResolver: (String) -> Type<*>?): Type<*>? {
        if (!typeDef.startsWith("$wrapperName<")) return null

        val innerTypeDef = typeDef.removeSurrounding("$wrapperName<", ">")

        val innerType = typeResolver(innerTypeDef)

        return innerType?.let { createWrapper(typeDef, it) }
    }
}