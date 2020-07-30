package jp.co.soramitsu.fearless_utils.wsrpc

import com.google.gson.annotations.SerializedName

data class RpcResponse(
    @SerializedName("jsonrpc")
    val jsonrpc: String,
    @SerializedName("result")
    val result: Any,
    @SerializedName("id")
    val id: Int
)