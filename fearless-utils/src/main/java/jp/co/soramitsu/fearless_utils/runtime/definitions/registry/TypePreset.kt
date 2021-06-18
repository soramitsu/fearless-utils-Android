package jp.co.soramitsu.fearless_utils.runtime.definitions.registry

import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.*
import jp.co.soramitsu.fearless_utils.runtime.metadata.RuntimeMetadata
import jp.co.soramitsu.schema.TypePreset
import jp.co.soramitsu.schema.definitions.types.Type
import jp.co.soramitsu.schema.definitions.types.TypeReference
import jp.co.soramitsu.schema.definitions.types.composite.Alias
import jp.co.soramitsu.schema.definitions.types.primitives.*
import jp.co.soramitsu.schema.definitions.types.stub.FakeType

typealias TypePresetBuilder = MutableMap<String, TypeReference>

fun TypePreset.newBuilder(): TypePresetBuilder = toMutableMap()

fun TypePresetBuilder.type(type: Type<*>) {
    val currentRef = getOrCreate(type.name)

    currentRef.value = type
}

fun TypePresetBuilder.fakeType(name: String) {
    type(FakeType(name))
}

fun TypePresetBuilder.alias(alias: String, original: String) {
    val aliasedReference = getOrCreate(original)

    val typeAlias = Alias(alias, aliasedReference)

    type(typeAlias)
}

fun TypePresetBuilder.getOrCreate(definition: String) = getOrPut(definition) { TypeReference(null) }

fun TypePresetBuilder.create(definition: String): TypeReference =
    TypeReference(null).also { put(definition, it) }

fun createTypePresetBuilder(): TypePresetBuilder = mutableMapOf()

fun typePreset(builder: TypePresetBuilder.() -> Unit): TypePreset {
    return createTypePresetBuilder().apply(builder)
}

fun substratePreParsePreset(types: Map<String, Type<*>> = mapOf(), metadata: RuntimeMetadata): TypePreset = typePreset {
    type(BooleanType)

    type(u8)
    type(u16)
    type(u32)
    type(u64)
    type(u128)
    type(u256)

    type(GenericAccountId)
    type(Null)
    type(GenericCall(metadata))

    fakeType("GenericBlock")

    type(H160)
    type(H256)
    type(H512)

    alias("GenericVote", "u8")

    type(Bytes)
    type(BitVec)

    type(Extrinsic(metadata, types))

    type(CallBytes) // seems to be unused in runtime
    type(EraType)
    type(Data(this))

    alias("BoxProposal", "Proposal")

    type(GenericConsensusEngineId)

    type(SessionKeysSubstrate(this))

    alias("GenericAccountIndex", "u32")

    type(GenericMultiAddress(this))

    type(OpaqueCall(metadata))

    type(GenericEvent(metadata))
    type(EventRecord(this))

    alias("<T::Lookup as StaticLookup>::Source", "LookupSource")
    alias("U64", "u64")
    alias("U32", "u32")

    alias("Bidkind", "BidKind")

    alias("AccountIdAddress", "GenericAccountId")

    alias("i128", "u128")

    alias("VoteWeight", "u128")
    alias("PreRuntime", "GenericPreRuntime")
    // todo replace with real type
    fakeType("GenericPreRuntime")
    type(GenericSealV0(this))
    type(GenericSeal(this))
    type(GenericConsensus(this))
}
