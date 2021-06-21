package jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics

import jp.co.soramitsu.schema.TypePresetBuilder
import jp.co.soramitsu.schema.definitions.types.TypeReference
import jp.co.soramitsu.schema.definitions.types.composite.Struct
import jp.co.soramitsu.schema.definitions.types.composite.Vec
import jp.co.soramitsu.schema.definitions.types.primitives.FixedByteArray
import jp.co.soramitsu.schema.getOrCreate

val GenericConsensusEngineId = FixedByteArray("GenericConsensusEngineId", 4)

@Suppress("FunctionName")
fun GenericConsensus(typePresetBuilder: TypePresetBuilder) = Struct(
    name = "GenericConsensus",
    mapping = linkedMapOf(
        "engine" to typePresetBuilder.getOrCreate("ConsensusEngineId"),
        "data" to TypeReference(
            Vec(
                name = "Vec<u8>",
                typeReference = typePresetBuilder.getOrCreate("u8")
            )
        )
    )
)
