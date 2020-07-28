package jp.co.soramitsu.fearless_utils.encrypt

data class Keypair(val privateKey: ByteArray, val publicKey: ByteArray, val nonce: ByteArray = ByteArray(0))