package jp.co.soramitsu.fearless_utils.wsrpc

import com.google.gson.annotations.SerializedName

data class RpcRequest(
    @SerializedName("id")
    val id: Int,
    @SerializedName("method")
    val method: String,
    @SerializedName("params")
    val params: Array<String>,
    @SerializedName("jsonrpc")
    val jsonRpc: String = "2.0"
)