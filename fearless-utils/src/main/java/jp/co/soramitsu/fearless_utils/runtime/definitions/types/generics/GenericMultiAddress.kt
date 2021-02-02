package jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics

import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypePresetBuilder
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.getOrCreate
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.TypeReference
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.DictEnum
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.Compact

@Suppress("FunctionName")
fun GenericMultiAddress(typePresetBuilder: TypePresetBuilder) = DictEnum(
    name = "GenericMultiAddress",
    elements = listOf(
        DictEnum.Entry("Id", typePresetBuilder.getOrCreate("AccountId")),
        DictEnum.Entry("Index", TypeReference(Compact("Compact<AccountIndex>"))),
        DictEnum.Entry("Raw", typePresetBuilder.getOrCreate("Bytes")),
        DictEnum.Entry("Address32", typePresetBuilder.getOrCreate("H256")),
        DictEnum.Entry("Address20", typePresetBuilder.getOrCreate("H160"))
    )
)