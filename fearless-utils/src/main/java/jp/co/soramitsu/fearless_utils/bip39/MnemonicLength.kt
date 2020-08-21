package jp.co.soramitsu.fearless_utils.bip39

enum class MnemonicLength(val byteLength: Int) {
    TWELVE(16),
    FIFTEEN(20),
    EIGHTEEN(24),
    TWENTY_ONE(28),
    TWENTY_FOUR(32);
}