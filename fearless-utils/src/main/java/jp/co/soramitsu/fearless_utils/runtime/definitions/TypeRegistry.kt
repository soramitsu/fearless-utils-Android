package jp.co.soramitsu.fearless_utils.runtime.definitions

import jp.co.soramitsu.fearless_utils.extensions.tryFindNonNull
import jp.co.soramitsu.fearless_utils.runtime.definitions.extensions.CompactExtension
import jp.co.soramitsu.fearless_utils.runtime.definitions.extensions.FixedArrayExtension
import jp.co.soramitsu.fearless_utils.runtime.definitions.extensions.GenericsExtension
import jp.co.soramitsu.fearless_utils.runtime.definitions.extensions.OptionExtension
import jp.co.soramitsu.fearless_utils.runtime.definitions.extensions.TupleExtension
import jp.co.soramitsu.fearless_utils.runtime.definitions.extensions.VectorExtension
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.GenericAccountId
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.Null
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.BooleanType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.u128
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.u16
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.u256
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.u32
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.u64
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.u8
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.stub.FakeType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.stub.Stub
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.stub.TypeAlias

interface TypeConstructorExtension {

    fun createType(typeDef: String, typeResolver: (String) -> Type<*>?) : Type<*>?
}

interface PostProcessor {

    fun process(type: Type<*>) : Type<*>?
}

class TypeRegistry(
    initialTypes: Map<String, Type<*>> = mapOf(),
    initialExtensions: Set<TypeConstructorExtension> = setOf(),
    initialPostProcessors: Set<PostProcessor> = setOf(),
) {

    private val types: MutableMap<String, Type<*>> = initialTypes.toMutableMap()
    private val extensions = initialExtensions.toMutableSet()

    private val postProcessors = initialPostProcessors.toMutableSet()

    operator fun get(definition: String): Type<*>? {
        val fromTypes = types[definition]

        if (fromTypes != null) return fromTypes

        return resolveFromExtensions(definition)
    }

    inline operator fun <reified R> get(key: String): R? = get(key) as? R

    operator fun set(definition: String, type: Type<*>) {
        types[definition] = type
    }

    operator fun plusAssign(other: TypeRegistry) {
        types += other.types
        extensions += other.extensions
    }

    operator fun plus(other: TypeRegistry): TypeRegistry {
        return TypeRegistry(
            initialTypes = types + other.types,
            initialExtensions = extensions + other.extensions
        )
    }

    fun registerType(type: Type<*>) {
        types[type.name] = type
    }

    fun registerFakeType(name: String) {
        registerType(FakeType(name))
    }

    fun registerAlias(alias: String, original: String) {
        types[alias] = TypeAlias(alias, original)
    }

    fun addExtension(extension: TypeConstructorExtension) {
        extensions += extension
    }

    fun resolveFromExtensions(
        typeDef: String,
        typeResolver: (String) -> Type<*>? = { get(it) }
    ): Type<*>? {
        return extensions.tryFindNonNull { it.createType(typeDef, typeResolver) }
    }

    fun removeStubs() {
        types.forEach { (name, type) ->
            val updated = type.replaceStubs(this)

            if (updated !== type) {
                types[name] = updated
            }
        }
    }

    fun all(): List<Pair<String, Type<*>>> = types.toList()
}

fun substrateBaseTypes(): TypeRegistry {
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
        addExtension(GenericsExtension)

        registerAlias("<T::Lookup as StaticLookup>::Source", "LookupSource")
        registerAlias("U64", "u64")
        registerAlias("U32", "u32")
    }
}

fun kusamaBaseTypes(): TypeRegistry {
    return TypeRegistry().apply {
        registerFakeType("AccountIdAddress")
    }
}