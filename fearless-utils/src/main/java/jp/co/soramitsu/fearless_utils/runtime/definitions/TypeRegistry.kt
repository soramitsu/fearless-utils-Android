package jp.co.soramitsu.fearless_utils.runtime.definitions

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

class TypeRegistry(initialTypes: Map<String, Type<*>> = mapOf()) {

    private val types: MutableMap<String, Type<*>> = initialTypes.toMutableMap()

    operator fun get(definition: String) = types[definition]

    inline operator fun <reified R> get(key: String): R? = get(key) as? R

    operator fun set(definition: String, type: Type<*>) {
        types[definition] = type
    }

    operator fun plusAssign(other: TypeRegistry) {
        types += other.types
    }

    operator fun plus(other: TypeRegistry): TypeRegistry {
        return TypeRegistry(types + other.types)
    }

    fun registerType(type: Type<*>) {
        types[type.name] = type
    }

    fun registerFakeType(name: String) {
        registerType(FakeType(name))
    }

    fun registerAlias(original: String, alias: String) {
        val type = types[original]
            ?: throw IllegalArgumentException("$original was not found in the registry")

        types[alias] = type
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

        registerAlias("u64", "U64")
        registerAlias("u32", "U32")

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
    }
}

fun kusamaBaseTypes(): TypeRegistry {
    return TypeRegistry().apply {
        registerFakeType("AccountIdAddress")
    }
}