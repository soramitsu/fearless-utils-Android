package jp.co.soramitsu.fearless_utils.ss58

enum class NetworkType(
    val addressByte: Byte
) {
    KUSAMA(2.toByte()),
    POLKADOT(0.toByte())
}