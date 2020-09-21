package jp.co.soramitsu.fearless_utils.encrypt

enum class EncryptionType(val rawName: String) {
    SR25519("sr25519"),
    ED25519("ed25519"),
    ECDSA("ecdsa");

    companion object {
        fun fromString(string: String): EncryptionType {
            return when (string) {
                SR25519.rawName -> SR25519
                ECDSA.rawName -> ECDSA
                ED25519.rawName -> ED25519
                else -> throw JsonSeedDecodingException.UnsupportedEncryptionTypeException()
            }
        }
    }
}