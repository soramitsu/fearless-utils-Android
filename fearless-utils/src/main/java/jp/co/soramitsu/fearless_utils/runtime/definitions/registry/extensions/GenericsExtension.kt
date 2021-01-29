package jp.co.soramitsu.fearless_utils.runtime.definitions.registry.extensions

import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypeConstructorExtension
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type

private val GENERIC_REGEX = "^([^<]*)<(.+)>\$".toRegex() // PartName<SubType>

private const val RAW_TYPE_GROUP_INDEX = 1 // first one will be the entire typeDef, the second one will be raw type

object GenericsExtension : TypeConstructorExtension {

    override fun createType(name: String, typeDef: String, registry: TypeRegistry): Type<*>? {
        val groups = GENERIC_REGEX.find(typeDef)?.groupValues ?: return null
        val rawType = groups.getOrNull(RAW_TYPE_GROUP_INDEX) ?: return null

        return registry[rawType]
    }
}