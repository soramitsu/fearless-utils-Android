package jp.co.soramitsu.fearless_utils.runtime.definitions.extensions

import jp.co.soramitsu.fearless_utils.runtime.definitions.TypeConstructorExtension
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type

private val GENERIC_REGEX = "^([^<]*)<(.+)>\$".toRegex() // PartName<SubType>

private const val RAW_TYPE_GROUP_INDEX = 1 // first one will be the entire typeDef, the second one will be raw type

object GenericsExtension : TypeConstructorExtension {

    override fun createType(typeDef: String, typeResolver: (String) -> Type<*>?): Type<*>? {
        val groups = GENERIC_REGEX.find(typeDef)?.groupValues ?: return null
        val rawType = groups.getOrNull(RAW_TYPE_GROUP_INDEX) ?: return null

        return typeResolver(rawType)
    }
}