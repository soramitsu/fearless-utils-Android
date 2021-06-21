package jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics

import jp.co.soramitsu.schema.TypePresetBuilder
import jp.co.soramitsu.schema.definitions.types.TypeReference
import jp.co.soramitsu.schema.definitions.types.composite.DictEnum
import jp.co.soramitsu.schema.definitions.types.primitives.Compact
import jp.co.soramitsu.schema.getOrCreate

const val MULTI_ADDRESS_ID = "Id"

@Suppress("FunctionName")
fun GenericMultiAddress(typePresetBuilder: TypePresetBuilder) = DictEnum(
    name = "GenericMultiAddress",
    elements = listOf(
        DictEnum.Entry(MULTI_ADDRESS_ID, typePresetBuilder.getOrCreate("AccountId")),
        DictEnum.Entry("Index", TypeReference(Compact("Compact<AccountIndex>"))),
        DictEnum.Entry("Raw", typePresetBuilder.getOrCreate("Bytes")),
        DictEnum.Entry("Address32", typePresetBuilder.getOrCreate("H256")),
        DictEnum.Entry("Address20", typePresetBuilder.getOrCreate("H160"))
    )
)
