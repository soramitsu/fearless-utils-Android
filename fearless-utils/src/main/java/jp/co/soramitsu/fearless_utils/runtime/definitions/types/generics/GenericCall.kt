package jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.errors.EncodeDecodeException
import jp.co.soramitsu.fearless_utils.runtime.metadata.Function
import jp.co.soramitsu.fearless_utils.runtime.metadata.FunctionArgument
import jp.co.soramitsu.fearless_utils.runtime.metadata.callOrNull
import jp.co.soramitsu.fearless_utils.runtime.metadata.moduleOrNull
import jp.co.soramitsu.fearless_utils.scale.dataType.tuple
import jp.co.soramitsu.fearless_utils.scale.dataType.uint8

object GenericCall : Type<GenericCall.Instance>("GenericCall") {

    class Instance(val moduleIndex: Int, val callIndex: Int, val arguments: Map<String, Any?>)

    private val indexCoder = tuple(uint8, uint8)

    override val isFullyResolved = true

    override fun decode(scaleCodecReader: ScaleCodecReader, runtime: RuntimeSnapshot): Instance {
        val (moduleIndex, callIndex) = indexCoder.read(scaleCodecReader)
            .run { first.toInt() to second.toInt() }

        val call = getCallOrThrow(runtime, moduleIndex, callIndex)

        val arguments = call.arguments.associate { argumentDefinition ->
            argumentDefinition.name to argumentDefinition.typeOrThrow()
                .decode(scaleCodecReader, runtime)
        }

        return Instance(moduleIndex, callIndex, arguments)
    }

    override fun encode(
        scaleCodecWriter: ScaleCodecWriter,
        runtime: RuntimeSnapshot,
        value: Instance
    ) = with(value) {
        val call = getCallOrThrow(runtime, moduleIndex, callIndex)

        indexCoder.write(scaleCodecWriter, moduleIndex.toUByte() to callIndex.toUByte())

        call.arguments.forEach { argumentDefinition ->
            argumentDefinition.typeOrThrow()
                .encodeUnsafe(scaleCodecWriter, runtime, arguments[argumentDefinition.name])
        }
    }

    override fun isValidInstance(instance: Any?): Boolean {
        return instance is Instance
    }

    private fun FunctionArgument.typeOrThrow() =
        type ?: throw EncodeDecodeException("Argument $name is not resolved")

    private fun getCallOrThrow(
        runtime: RuntimeSnapshot,
        moduleIndex: Int,
        callIndex: Int
    ): Function {
        return runtime.metadata.moduleOrNull(moduleIndex)?.callOrNull(callIndex) ?: callNotFound(
            moduleIndex,
            callIndex
        )
    }

    private fun callNotFound(moduleIndex: Int, callIndex: Int): Nothing {
        throw EncodeDecodeException("No call found for index ($moduleIndex, $callIndex)")
    }
}