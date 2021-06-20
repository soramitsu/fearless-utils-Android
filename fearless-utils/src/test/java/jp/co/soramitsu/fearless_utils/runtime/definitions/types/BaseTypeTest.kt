package jp.co.soramitsu.fearless_utils.runtime.definitions.types

import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.substratePreParsePreset
import jp.co.soramitsu.fearless_utils.runtime.metadata.*
import jp.co.soramitsu.fearless_utils.runtime.metadata.Function
import jp.co.soramitsu.schema.definitions.types.primitives.BooleanType
import jp.co.soramitsu.schema.definitions.types.primitives.u8
import java.math.BigInteger

abstract class BaseTypeTest {

    protected val runtime: RuntimeSnapshot = RuntimeSnapshot()
        .also { it.typeRegistry.types = substratePreParsePreset(it) }
        .also { it.metadata = meta() }

    fun meta(): RuntimeMetadata {
        return RuntimeMetadata(
            runtimeVersion = BigInteger.ONE,
            modules = mapOf(
                "A" to Module(
                    name = "A",
                    storage = Storage("_A", emptyMap()),
                    calls = mapOf(
                        "B" to Function(
                            name = "B",
                            arguments = listOf(
                                FunctionArgument(
                                    name = "arg1",
                                    type = BooleanType
                                ),
                                FunctionArgument(
                                    name = "arg2",
                                    type = u8
                                )
                            ),
                            documentation = emptyList(),
                            index = 0 to 0
                        )
                    ),
                    events = mapOf(
                        "A" to Event(
                            name = "A",
                            arguments = listOf(
                                BooleanType,
                                u8
                            ),
                            documentation = emptyList(),
                            index = 0 to 0
                        )
                    ),
                    constants = emptyMap(),
                    errors = emptyMap(),
                    index = BigInteger.ONE
                )
            ),
            extrinsic = ExtrinsicMetadata(
                version = BigInteger.ONE,
                signedExtensions = emptyList()
            )
        )
    }

}
