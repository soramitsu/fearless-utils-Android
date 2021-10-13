package jp.co.soramitsu.fearless_utils.encrypt

sealed class SignatureWrapper {
    abstract val signature: ByteArray

    class Ecdsa(
        val v: ByteArray,
        val r: ByteArray,
        val s: ByteArray
    ) : SignatureWrapper() {

        override val signature: ByteArray = r + s + v
    }

    class Other(override val signature: ByteArray) : SignatureWrapper()
}
