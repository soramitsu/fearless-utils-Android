package jp.co.soramitsu.fearless_utils.wsrpc.request

import com.google.gson.annotations.SerializedName

open class RpcRequest(
    @SerializedName("jsonrpc")
    val jsonRpc: String = "2.0"
)