package jp.co.soramitsu.fearless_utils.wsrpc

import com.google.gson.Gson
import com.neovisionaries.ws.client.WebSocketFactory
import com.neovisionaries.ws.client.WebSocketState
import jp.co.soramitsu.fearless_utils.wsrpc.exception.ConnectionClosedException
import jp.co.soramitsu.fearless_utils.wsrpc.logging.Logger
import jp.co.soramitsu.fearless_utils.wsrpc.mappers.nonNull
import jp.co.soramitsu.fearless_utils.wsrpc.mappers.string
import jp.co.soramitsu.fearless_utils.wsrpc.recovery.Reconnector
import jp.co.soramitsu.fearless_utils.wsrpc.request.DeliveryType
import jp.co.soramitsu.fearless_utils.wsrpc.request.RequestExecutor
import jp.co.soramitsu.fearless_utils.wsrpc.request.RespondableSendable
import jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.RuntimeRequest
import jp.co.soramitsu.fearless_utils.wsrpc.response.RpcResponse
import jp.co.soramitsu.fearless_utils.wsrpc.socket.ObservableState
import jp.co.soramitsu.fearless_utils.wsrpc.socket.RpcSocket
import jp.co.soramitsu.fearless_utils.wsrpc.socket.RpcSocketListener
import jp.co.soramitsu.fearless_utils.wsrpc.socket.StateObserver
import jp.co.soramitsu.fearless_utils.wsrpc.state.SocketStateMachine
import jp.co.soramitsu.fearless_utils.wsrpc.state.SocketStateMachine.Event
import jp.co.soramitsu.fearless_utils.wsrpc.state.SocketStateMachine.SideEffect
import jp.co.soramitsu.fearless_utils.wsrpc.state.SocketStateMachine.State
import jp.co.soramitsu.fearless_utils.wsrpc.subscription.RespondableSubscription
import jp.co.soramitsu.fearless_utils.wsrpc.subscription.response.SubscriptionChange

class SocketService(
    private val jsonMapper: Gson,
    private val logger: Logger,
    private val webSocketFactory: WebSocketFactory,
    private val reconnector: Reconnector,
    private val requestExecutor: RequestExecutor
) : RpcSocketListener {

    private var socket: RpcSocket? = null

    private val stateContainer = ObservableState(initialState = State.Disconnected)

    fun switchUrl(url: String) {
        stop()

        start(url)
    }

    fun started() = stateContainer.getState() !is State.Disconnected

    @Synchronized
    fun start(url: String) {
        updateState(Event.Start(url))
    }

    @Synchronized
    fun stop() {
        updateState(Event.Stop)
    }

    fun addStateObserver(observer: StateObserver) = stateContainer.addObserver(observer)

    fun removeStateObserver(observer: StateObserver) = stateContainer.removeObserver(observer)

    @Synchronized
    fun subscribe(
        request: RuntimeRequest,
        callback: ResponseListener<SubscriptionChange>
    ): Cancellable {
        return executeRequest(
            request,
            DeliveryType.ON_RECONNECT,
            SubscribedCallback(request.id, callback)
        )
    }

    @Synchronized
    fun executeRequest(
        runtimeRequest: RuntimeRequest,
        deliveryType: DeliveryType = DeliveryType.AT_LEAST_ONCE,
        callback: ResponseListener<RpcResponse>
    ): Cancellable {
        val sendable = RespondableSendable(runtimeRequest, deliveryType, callback)

        updateState(Event.Send(sendable))

        return RequestCancellable(sendable)
    }

    @Synchronized
    override fun onResponse(rpcResponse: RpcResponse) {
        updateState(Event.SendableResponse(rpcResponse))
    }

    override fun onResponse(subscriptionChange: SubscriptionChange) {
        updateState(Event.SubscriptionResponse(subscriptionChange))
    }

    @Synchronized
    override fun onConnected() {
        updateState(Event.Connected)
    }

    @Synchronized
    override fun onStateChanged(newState: WebSocketState) {
        if (newState == WebSocketState.CLOSED) {
            updateState(Event.ConnectionError(ConnectionClosedException()))
        }
    }

    @Synchronized
    private fun updateState(event: Event) {
        val state = stateContainer.getState()
        val (newState, sideEffects) = SocketStateMachine.transition(state, event)
        stateContainer.setState(newState)

        logger.log("[STATE MACHINE][TRANSITION] $event : $state -> $newState")

        sideEffects.forEach(::consumeSideEffect)
    }

    private fun consumeSideEffect(sideEffect: SideEffect) {
        logger.log("[STATE MACHINE][SIDE EFFECT] $sideEffect")

        when (sideEffect) {
            is SideEffect.ResponseToSendable -> respondToRequest(
                sideEffect.sendable,
                sideEffect.response
            )
            is SideEffect.RespondSendablesError -> respondError(
                sideEffect.sendables,
                sideEffect.error
            )
            is SideEffect.RespondToSubscription -> respondToSubscription(
                sideEffect.subscription,
                sideEffect.change
            )
            is SideEffect.SendSendables -> sendToSocket(sideEffect.sendables)
            is SideEffect.Connect -> connect(sideEffect.url)
            is SideEffect.ScheduleReconnect -> scheduleReconnect(sideEffect.attempt, sideEffect.url)
            is SideEffect.Disconnect -> disconnect()
            is SideEffect.Unsubscribe -> unsubscribe()
        }
    }

    private fun respondToRequest(
        sendable: SocketStateMachine.Sendable,
        response: RpcResponse
    ) {
        require(sendable is RespondableSendable)

        sendable.callback.onNext(response)
    }

    private fun respondError(sendables: Set<SocketStateMachine.Sendable>, throwable: Throwable) {
        sendables.forEach {
            require(it is RespondableSendable)

            it.callback.onError(throwable)
        }
    }

    private fun respondToSubscription(
        subscription: SocketStateMachine.Subscription,
        response: SubscriptionChange
    ) {
        require(subscription is RespondableSubscription)

        subscription.callback.onNext(response)
    }

    private fun sendToSocket(sendables: Set<SocketStateMachine.Sendable>) {
        requestExecutor.execute {
            sendables.forEach {
                require(it is RespondableSendable)

                socket!!.sendRpcRequest(it.request)
            }
        }
    }

    private fun connect(url: String) = reconnector.connect(url, ::connectToSocket)

    private fun scheduleReconnect(attempt: Int, url: String) =
        reconnector.scheduleConnect(attempt, url, ::connectToSocket)

    private fun connectToSocket(url: String) {
        socket = createSocket(url)
        socket!!.connectAsync()
    }

    private fun createSocket(url: String) =
        RpcSocket(url, this, logger, webSocketFactory, jsonMapper)

    private fun disconnect() {
        socket!!.clearListeners()
        socket!!.disconnect()
        socket = null

        requestExecutor.reset()
        reconnector.reset()
    }

    private fun unsubscribe() {
        // TODO
    }

    interface ResponseListener<R> {
        fun onNext(response: R)

        fun onError(throwable: Throwable)
    }

    interface Cancellable {
        fun cancel()
    }

    inner class SubscribedCallback(
        private val initiatorId: Int,
        private val subscriptionCallback: ResponseListener<SubscriptionChange>
    ) : ResponseListener<RpcResponse> {

        override fun onNext(response: RpcResponse) {
            val id = try {
                string().nonNull().map(response, jsonMapper)
            } catch (e: Exception) {
                subscriptionCallback.onError(e)

                return
            }

            val subscription = RespondableSubscription(id, initiatorId, subscriptionCallback)

            updateState(Event.Subscribed(subscription))
        }

        override fun onError(throwable: Throwable) {
            subscriptionCallback.onError(throwable)
        }
    }

    inner class RequestCancellable(
        private val sendable: SocketStateMachine.Sendable
    ) : Cancellable {

        override fun cancel() {
            updateState(Event.Cancel(sendable))
        }
    }
}
