package jp.co.soramitsu.schema.definitions.types.generics

import jp.co.soramitsu.schema.definitions.registry.TypePresetBuilder
import jp.co.soramitsu.schema.definitions.registry.getOrCreate
import jp.co.soramitsu.schema.definitions.types.TypeReference
import jp.co.soramitsu.schema.definitions.types.composite.Struct
import jp.co.soramitsu.schema.definitions.types.composite.Vec

@Suppress("FunctionName")
fun EventRecord(typePresetBuilder: TypePresetBuilder) = Struct(
    name = "EventRecord",
    mapping = linkedMapOf(
        "phase" to typePresetBuilder.getOrCreate("Phase"),
        "event" to typePresetBuilder.getOrCreate("GenericEvent"),
        "topics" to TypeReference(
            Vec(
                name = "Vec<Hash>",
                typeReference = typePresetBuilder.getOrCreate("Hash")
            )
        )
    )
)