package jp.co.soramitsu.fearless_utils.encrypt.model

import com.google.gson.annotations.SerializedName

data class JsonAccountData(
    @SerializedName("address")
    val address: String,
    @SerializedName("encoded")
    val encoded: String,
    @SerializedName("encoding")
    val encoding: Encoding,
    @SerializedName("meta")
    val meta: Meta
) {
    data class Encoding(
        @SerializedName("content")
        val content: List<String>,
        @SerializedName("type")
        val type: List<String>,
        @SerializedName("version")
        val version: Int
    )

    data class Meta(
        @SerializedName("name")
        val name: String,
        @SerializedName("whenCreated")
        val createdOn: Long
    )
}
