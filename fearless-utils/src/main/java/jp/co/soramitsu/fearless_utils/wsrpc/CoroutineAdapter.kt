@file:Suppress("EXPERIMENTAL_API_USAGE")

package jp.co.soramitsu.fearless_utils.wsrpc

import jp.co.soramitsu.fearless_utils.wsrpc.request.DeliveryType
import jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.RuntimeRequest
import jp.co.soramitsu.fearless_utils.wsrpc.response.RpcResponse
import jp.co.soramitsu.fearless_utils.wsrpc.socket.StateObserver
import jp.co.soramitsu.fearless_utils.wsrpc.state.SocketStateMachine.State
import jp.co.soramitsu.fearless_utils.wsrpc.subscription.response.SubscriptionChange
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun SocketService.executeAsync(
    request: RuntimeRequest,
    deliveryType: DeliveryType = DeliveryType.AT_LEAST_ONCE
) = suspendCancellableCoroutine<RpcResponse> { cont ->
    val cancellable = executeRequest(request, deliveryType, object : SocketService.ResponseListener<RpcResponse> {
            override fun onNext(response: RpcResponse) {
                cont.resume(response)
            }

            override fun onError(throwable: Throwable) {
                cont.resumeWithException(throwable)
            }
        })

    cont.invokeOnCancellation {
        cancellable.cancel()
    }
}

fun SocketService.subscriptionFlow(request: RuntimeRequest): Flow<SubscriptionChange> =
    callbackFlow {
        val cancellable = subscribe(request, object : SocketService.ResponseListener<SubscriptionChange> {
                override fun onNext(response: SubscriptionChange) {
                    offer(response)
                }

                override fun onError(throwable: Throwable) {
                    close(throwable)
                }
            })

        awaitClose {
            cancellable.cancel()
        }
    }

fun SocketService.networkStateFlow(): Flow<State> = callbackFlow {
    val observer: StateObserver = { state: State ->
        offer(state)
    }

    addStateObserver(observer)

    awaitClose {
        removeStateObserver(observer)
    }
}