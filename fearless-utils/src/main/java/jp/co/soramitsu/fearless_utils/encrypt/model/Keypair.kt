package jp.co.soramitsu.fearless_utils.encrypt.model

class Keypair(
    val privateKey: ByteArray,
    val publicKey: ByteArray,
    val nonce: ByteArray? = null
)