package jp.co.soramitsu.fearless_utils.encrypt

/**
 * Pararams v, r, s refer to the corresponding components of ECDSA signature
 * Fore more information, see {@link Signer.signEcdca}
 **/
data class SignatureWrapper(
    val signature: ByteArray? = null,
    val v: ByteArray? = null,
    val r: ByteArray? = null,
    val s: ByteArray? = null
)