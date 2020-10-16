package jp.co.soramitsu.fearless_utils.wsrpc.request.runtime

import jp.co.soramitsu.fearless_utils.wsrpc.request.base.RpcRequest
import kotlin.random.Random

enum class Module(val id: String) {
    System("System")
}

private fun nextId() = Random.nextInt(1, Int.MAX_VALUE)

abstract class RuntimeRequest(
    val method: String,
    val params: List<Any>
) : RpcRequest() {
    val id: Int = nextId()
}