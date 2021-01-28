package jp.co.soramitsu.fearless_utils.runtime.definitions.registry

import jp.co.soramitsu.fearless_utils.extensions.tryFindNonNull
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.extensions.CompactExtension
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.extensions.FixedArrayExtension
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.extensions.OptionExtension
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.extensions.TupleExtension
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.extensions.VectorExtension
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.preprocessors.RemoveGenericNoisePreprocessor
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.TypeReference
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Alias
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.GenericAccountId
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.Null
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.BooleanType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.u128
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.u16
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.u256
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.u32
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.u64
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.u8
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.resolveAliasing
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.stub.FakeType

interface TypeConstructorExtension {

    fun createType(name: String, typeDef: String, registry: TypeRegistry): Type<*>?
}

interface RequestPreprocessor {

    fun process(definition: String): String
}

class TypeRegistry(
    initialTypes: Map<String, TypeReference> = mapOf(),
    initialExtensions: Set<TypeConstructorExtension> = setOf(),
    initialPreprocessors: Set<RequestPreprocessor> = setOf()
) {

    private val types: MutableMap<String, TypeReference> = initialTypes.toMutableMap()
    private val extensions = initialExtensions.toMutableSet()
    private val preprocessors = initialPreprocessors.toMutableSet()

    operator fun get(
        definition: String,
        resolveAliasing: Boolean = true,
        storageOnly: Boolean = false,
    ): Type<*>? {
        val typeRef = getTypeReference(definition, resolveAliasing, storageOnly)

        return typeRef.value
    }

    inline operator fun <reified R> get(
        key: String,
        resolveAliasing: Boolean = true,
        storageOnly: Boolean = false,
    ): R? {
        val type = get(key, resolveAliasing, storageOnly)

        return type?.let { it as R }
    }

    operator fun set(definition: String, type: Type<*>) {
        val preprocessed = applyPreprocessors(definition)

        val cachedReference = types.getTypeReference(preprocessed)

        cachedReference.value = type
    }

    operator fun plusAssign(other: TypeRegistry) {
        types += other.types
        extensions += other.extensions
    }

    operator fun plus(other: TypeRegistry): TypeRegistry {
        return TypeRegistry(
            initialTypes = types + other.types,
            initialExtensions = extensions + other.extensions,
            initialPreprocessors = preprocessors + other.preprocessors
        )
    }

    fun registerType(type: Type<*>) {
        set(type.name, type)
    }

    fun registerFakeType(name: String) {
        registerType(FakeType(name))
    }

    fun registerAlias(alias: String, original: String) {
        val aliasedReference = getTypeReference(original)

        val typeAlias = Alias(alias, aliasedReference)

        registerType(typeAlias)
    }

    fun addExtension(extension: TypeConstructorExtension) {
        extensions += extension
    }

    fun addPreprocessor(preprocessor: RequestPreprocessor) {
        preprocessors += preprocessor
    }

    fun resolveFromExtensions(name: String, typeDef: String): Type<*>? {
        val preprocessed = applyPreprocessors(typeDef)

        return extensions.tryFindNonNull { it.createType(name, preprocessed, this) }
    }

    fun getTypeReference(
        definition: String,
        resolveAliasing: Boolean = true,
        storageOnly: Boolean = false
    ): TypeReference {
        val preprocessed = applyPreprocessors(definition)

        val cachedTypeReference = types.getTypeReference(preprocessed)

        if (cachedTypeReference.value != null || storageOnly) {
            return cachedTypeReference.maybeResolveAliasing(resolveAliasing)
        }

        val resolvedFromExtensions = resolveFromExtensions(preprocessed, preprocessed)

        cachedTypeReference.value = resolvedFromExtensions

        return cachedTypeReference.maybeResolveAliasing(resolveAliasing)
    }

    fun allTypeRefs() = types.toList()

    private fun applyPreprocessors(requestDef: String): String {
        return preprocessors.fold(requestDef) { acc, preprocessor -> preprocessor.process(acc) }
    }
}

fun TypeRegistry.copy() = this + TypeRegistry()

private fun TypeReference.maybeResolveAliasing(resolveAliasing: Boolean) : TypeReference {
    return if (resolveAliasing) resolveAliasing() else this
}

fun substrateRegistryPreset(): TypeRegistry {
    return TypeRegistry().apply {
        registerType(BooleanType)

        registerType(u8)
        registerType(u16)
        registerType(u32)
        registerType(u64)
        registerType(u128)
        registerType(u256)

        registerType(GenericAccountId)

        registerType(Null)

        registerFakeType("GenericBlock")

        registerFakeType("GenericCall")
        registerFakeType("H160")
        registerFakeType("H256")
        registerFakeType("H512")
        registerFakeType("GenericVote")
        registerFakeType("Bytes")
        registerFakeType("BitVec")
        registerFakeType("ExtrinsicsDecoder")
        registerFakeType("CallBytes")
        registerFakeType("Era")
        registerFakeType("Data")
        registerFakeType("BoxProposal")
        registerFakeType("GenericConsensusEngineId")
        registerFakeType("SessionKeysSubstrate")
        registerFakeType("GenericMultiAddress")
        registerFakeType("OpaqueCall") // declared as "OpaqueCall": "OpaqueCall"
        registerFakeType("GenericAccountIndex") // declared as "OpaqueCall": "OpaqueCall"
        registerFakeType("GenericEvent") // declared as "OpaqueCall": "OpaqueCall"
        registerFakeType("EventRecord") // "EventRecord": "EventRecord"

        addExtension(VectorExtension)
        addExtension(CompactExtension)
        addExtension(OptionExtension)
        addExtension(FixedArrayExtension)
        addExtension(TupleExtension)

        addPreprocessor(RemoveGenericNoisePreprocessor)

        registerAlias("<T::Lookup as StaticLookup>::Source", "LookupSource")
        registerAlias("U64", "u64")
        registerAlias("U32", "u32")

        registerAlias("Bidkind", "BidKind")
    }
}

fun kusamaBaseTypes(): TypeRegistry {
    return TypeRegistry().apply {
        registerFakeType("AccountIdAddress")
    }
}

internal fun MutableMap<String, TypeReference>.getTypeReference(key: String): TypeReference {
    return getOrPut(key) { TypeReference(null) }
}
