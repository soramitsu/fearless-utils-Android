package jp.co.soramitsu.fearless_utils_android.ss58

enum class AddressType(val addressByte: Byte) {
    KUSAMA(2.toByte()),
    POLKADOT(0.toByte())
}