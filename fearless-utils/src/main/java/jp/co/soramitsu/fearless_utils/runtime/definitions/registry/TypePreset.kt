package jp.co.soramitsu.fearless_utils.runtime.definitions.registry

import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.*
import jp.co.soramitsu.schema.*
import jp.co.soramitsu.schema.definitions.types.composite.Alias
import jp.co.soramitsu.schema.definitions.types.primitives.*
import jp.co.soramitsu.schema.definitions.types.stub.FakeType

fun TypePresetBuilder.fakeType(name: String) {
    type(FakeType(name))
}

fun TypePresetBuilder.alias(alias: String, original: String) {
    val aliasedReference = getOrCreate(original)

    val typeAlias = Alias(alias, aliasedReference)

    type(typeAlias)
}

fun substratePreParsePreset(runtime: RuntimeSnapshot = RuntimeSnapshot()): TypePreset = typePreset {
    type(BooleanType)

    type(u8)
    type(u16)
    type(u32)
    type(u64)
    type(u128)
    type(u256)

    type(GenericAccountId)
    type(Null)
    type(GenericCall(runtime))

    fakeType("GenericBlock")

    type(H160)
    type(H256)
    type(H512)

    alias("GenericVote", "u8")

    type(Bytes)
    type(BitVec)

    type(Extrinsic(runtime))

    type(CallBytes) // seems to be unused in runtime
    type(EraType)
    type(Data(this))

    alias("BoxProposal", "Proposal")

    type(GenericConsensusEngineId)

    type(SessionKeysSubstrate(this))

    alias("GenericAccountIndex", "u32")

    type(GenericMultiAddress(this))

    type(OpaqueCall(runtime))

    type(GenericEvent(runtime))
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
