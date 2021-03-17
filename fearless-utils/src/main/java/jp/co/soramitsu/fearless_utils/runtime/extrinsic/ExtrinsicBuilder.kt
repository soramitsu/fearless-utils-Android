package jp.co.soramitsu.fearless_utils.runtime.extrinsic

import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType
import jp.co.soramitsu.fearless_utils.encrypt.Signer
import jp.co.soramitsu.fearless_utils.encrypt.model.Keypair
import jp.co.soramitsu.fearless_utils.hash.Hasher.blake2b256
import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.AdditionalExtras
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.Era
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.Extrinsic
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.ExtrinsicPayloadExtrasInstance
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.GenericCall
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.MultiSignature
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.SignedExtras
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.new
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.toHex
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.useScaleWriter
import jp.co.soramitsu.fearless_utils.runtime.metadata.call
import jp.co.soramitsu.fearless_utils.runtime.metadata.module
import jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.chain.RuntimeVersion
import java.math.BigInteger

private val DEFAULT_TIP = BigInteger.ZERO

private const val PAYLOAD_HASH_THRESHOLD = 256

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
        val signedExtrasInstance = mapOf(
            SignedExtras.ERA to era,
            SignedExtras.NONCE to nonce,
            SignedExtras.TIP to tip
        )

        val additionalExtrasInstance = mapOf(
            AdditionalExtras.BLOCK_HASH to blockHash,
            AdditionalExtras.GENESIS to genesisHash,
            AdditionalExtras.SPEC_VERSION to runtimeVersion.specVersion.toBigInteger(),
            AdditionalExtras.TX_VERSION to runtimeVersion.transactionVersion.toBigInteger(),
        )

        val payloadBytes = useScaleWriter {
            GenericCall.encode(this, runtime, call)
            SignedExtras.encode(this, runtime, signedExtrasInstance)
            AdditionalExtras.encode(this, runtime, additionalExtrasInstance)
        }

        val messageToSign = if (payloadBytes.size > PAYLOAD_HASH_THRESHOLD) {
            payloadBytes.blake2b256()
        } else {
            payloadBytes
        }

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

    private fun buildSignedExtras(): ExtrinsicPayloadExtrasInstance = mapOf(
        SignedExtras.ERA to era,
        SignedExtras.TIP to tip,
        SignedExtras.NONCE to nonce
    )
}