package jp.co.soramitsu.fearless_utils.encrypt

data class SignatureWrapper(
    val signature: ByteArray? = null,
    val v: ByteArray? = null,
    val r: ByteArray? = null,
    val s: ByteArray? = null
)