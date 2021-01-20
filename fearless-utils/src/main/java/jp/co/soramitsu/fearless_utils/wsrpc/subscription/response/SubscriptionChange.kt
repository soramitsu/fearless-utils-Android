package jp.co.soramitsu.fearless_utils.wsrpc.subscription.response

data class Change(val value: String?)

class SubscriptionChange(
    val jsonrpc: String,
    val method: String,
    val params: Params
) {

    class Params(val result: Result, val subscription: String) {

        // changes are in format [[storage key, value], [..], ..]
        class Result(val block: String, val changes: List<List<String?>>) {
            fun getSingleChange() = Change(changes.first()[1])
        }
    }

    val subscriptionId: String
        get() = params.subscription

    override fun toString() = "SubscriptionChange(${params.subscription})"
}