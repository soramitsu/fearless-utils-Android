package jp.co.soramitsu.fearless_utils.encrypt.model

import com.google.gson.annotations.SerializedName
import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType

const val ENCODING_SCRYPT = "scrypt"
const val ENCODING_SALSA = "xsalsa20-poly1305"
const val ENCODING_PKCS8 = "pkcs8"

const val JSON_VERSION = 3

class JsonAccountData(
    @SerializedName("address")
    val address: String,
    @SerializedName("encoded")
    val encoded: String,
    @SerializedName("encoding")
    val encoding: Encoding,
    @SerializedName("meta")
    val meta: Meta
) {
    class Encoding(
        @SerializedName("content")
        val content: List<String>,
        @SerializedName("type")
        val type: List<String>,
        @SerializedName("version")
        val version: Int
    ) {
        companion object {
            fun default(encryptionType: EncryptionType) = Encoding(
                content = listOf(ENCODING_PKCS8, encryptionType.rawName),
                type = listOf(ENCODING_SCRYPT, ENCODING_SALSA),
                version = JSON_VERSION
            )
        }
    }

    class Meta(
        @SerializedName("name")
        val name: String?,
        @SerializedName("whenCreated")
        val createdOn: Long
    )
}
