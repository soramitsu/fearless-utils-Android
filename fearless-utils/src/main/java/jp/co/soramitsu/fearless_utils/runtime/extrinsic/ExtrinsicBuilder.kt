package jp.co.soramitsu.fearless_utils.runtime.extrinsic

import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType
import jp.co.soramitsu.fearless_utils.encrypt.Signer
import jp.co.soramitsu.fearless_utils.encrypt.model.Keypair
import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.bytes
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.Era
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.Extrinsic
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.GenericCall
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.MultiSignature
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.SignedExtras
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.SignedExtrasInstance
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.new
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.toHex
import jp.co.soramitsu.fearless_utils.runtime.metadata.call
import jp.co.soramitsu.fearless_utils.runtime.metadata.module
import jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.chain.RuntimeVersion
import java.math.BigInteger

private val DEFAULT_TIP = BigInteger.ZERO

class ExtrinsicBuilder(
    private val runtime: RuntimeSnapshot,
    private val keypair: Keypair,
    private val nonce: BigInteger,
    private val runtimeVersion: RuntimeVersion,
    private val genesisHash: ByteArray,
    private val encryptionType: EncryptionType,
    private val accountIdentifier: Any,
    private val blockHash: ByteArray = genesisHash,
    private val era: Era = Era.Immortal,
    private val tip: BigInteger = DEFAULT_TIP
) {

    private val calls = mutableListOf<GenericCall.Instance>()

    fun call(
        moduleIndex: Int,
        callIndex: Int,
        args: Map<String, Any?>
    ): ExtrinsicBuilder {
        calls.add(GenericCall.Instance(moduleIndex, callIndex, args))

        return this
    }

    fun call(
        moduleName: String,
        callName: String,
        arguments: Map<String, Any?>
    ): ExtrinsicBuilder {
        val call = runtime.metadata.module(moduleName).call(callName)
        val (moduleIndex, callIndex) = call.index

        calls.add(GenericCall.Instance(moduleIndex, callIndex, arguments))

        return this
    }

    fun build(): String {
        val call = maybeWrapInBatch()
        val multiSignature = buildSignature(call)
        val signedExtras = buildSignedExtras()

        val extrinsic = Extrinsic.Instance(
            signature = Extrinsic.Signature.new(
                accountIdentifier = accountIdentifier,
                signature = multiSignature,
                signedExtras = signedExtras
            ),
            call = call
        )

        return Extrinsic.toHex(runtime, extrinsic)
    }

    private fun maybeWrapInBatch(): GenericCall.Instance {
        return if (calls.size == 1) {
            calls.first()
        } else {
            wrapInBatch()
        }
    }

    private fun buildSignature(call: GenericCall.Instance): MultiSignature {
        val payloadType = runtime.typeRegistry["ExtrinsicPayloadValue"]
            ?: error("Cannot resolve ExtrinsicPayloadValue type")

        val callBytes = GenericCall.toHex(runtime, call)

        val payload = Struct.Instance(
            mapOf(
                "call" to callBytes,
                "era" to era,
                "nonce" to nonce,
                "tip" to tip,
                "specVersion" to runtimeVersion.specVersion.toBigInteger(),
                "transactionVersion" to runtimeVersion.transactionVersion.toBigInteger(),
                "genesisHash" to genesisHash,
                "blockHash" to blockHash
            )
        )

        val messageToSign = payloadType.bytes(runtime, payload)
        val signature = Signer.sign(encryptionType, messageToSign, keypair).signature

        return MultiSignature(encryptionType, signature)
    }

    private fun wrapInBatch(): GenericCall.Instance {
        val batchCall = runtime.metadata.module("Utility").call("batch")
        val (moduleIndex, callIndex) = batchCall.index

        return GenericCall.Instance(
            moduleIndex = moduleIndex,
            callIndex = callIndex,
            arguments = mapOf(
                "calls" to calls
            )
        )
    }

    private fun buildSignedExtras(): SignedExtrasInstance = mapOf(
        SignedExtras.ERA to era,
        SignedExtras.TIP to tip,
        SignedExtras.NONCE to nonce
    )
}