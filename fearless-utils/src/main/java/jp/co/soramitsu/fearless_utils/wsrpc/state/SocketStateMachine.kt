package jp.co.soramitsu.fearless_utils.wsrpc.state

import jp.co.soramitsu.fearless_utils.wsrpc.request.DeliveryType
import jp.co.soramitsu.fearless_utils.wsrpc.response.RpcResponse
import jp.co.soramitsu.fearless_utils.wsrpc.subscription.response.SubscriptionChange

typealias Transition = Pair<SocketStateMachine.State, List<SocketStateMachine.SideEffect>>

object SocketStateMachine {

    interface Sendable {
        val id: Int

        val deliveryType: DeliveryType
    }

    interface Subscription {
        val id: String

        val initiatorId: Int
    }

    sealed class State {
        data class WaitingForReconnect(
            val url: String,
            val attempt: Int = 0,
            internal val pendingSendables: Set<Sendable>
        ) : State()

        data class Connecting(
            val url: String,
            val attempt: Int = 0,
            internal val pendingSendables: Set<Sendable> = emptySet()
        ) :
            State()

        data class Connected(
            val url: String,
            internal val toResendOnReconnect: Set<Sendable>,
            internal val waitingForResponse: Set<Sendable>,
            internal val subscriptions: Set<Subscription>
        ) : State()

        object Disconnected : State()

        override fun toString(): String = javaClass.simpleName
    }

    sealed class Event {
        data class Send(val sendable: Sendable) : Event()

        data class Cancel(val sendable: Sendable) : Event()

        data class SendableResponse(val response: RpcResponse) : Event()

        data class Subscribed(val subscription: Subscription) : Event()

        data class SubscriptionResponse(val response: SubscriptionChange) : Event()

        data class ConnectionError(val throwable: Throwable) : Event()

        object Connected : Event()

        object ReadyToReconnect : Event()

        object Stop : Event()

        class Start(val url: String) : Event()

        override fun toString(): String = javaClass.simpleName
    }

    sealed class SideEffect {

        data class ResponseToSendable(val sendable: Sendable, val response: RpcResponse) :
            SideEffect()

        /**
         * For [DeliveryType.AT_MOST_ONCE] errors
         */
        data class RespondSendablesError(
            val sendables: Set<Sendable>,
            val error: Throwable
        ) : SideEffect()

        data class RespondToSubscription(
            val subscription: Subscription,
            val change: SubscriptionChange
        ) : SideEffect()

        data class Unsubscribe(val subscription: Subscription) : SideEffect()

        data class SendSendables(val sendables: Set<Sendable>) : SideEffect()

        data class Connect(val url: String) : SideEffect()

        data class ScheduleReconnect(val attempt: Int) : SideEffect()

        object Disconnect : SideEffect()

        override fun toString(): String = javaClass.simpleName
    }

    fun initialState(): State = State.Disconnected

    fun transition(
        state: State,
        event: Event
    ): Transition {
        val sideEffects = mutableListOf<SideEffect>()

        val newState = when (state) {
            is State.WaitingForReconnect -> {
                when (event) {
                    is Event.ReadyToReconnect -> {
                        sideEffects += SideEffect.Connect(state.url)
                        State.Connecting(state.url, state.attempt, state.pendingSendables)
                    }
                    is Event.Send -> {
                        sideEffects += SideEffect.Connect(state.url)
                        State.Connecting(
                            url = state.url,
                            attempt = 0,
                            pendingSendables = state.pendingSendables + event.sendable
                        )
                    }
                    is Event.Cancel -> {
                        state.copy(pendingSendables = state.pendingSendables - event.sendable)
                    }
                    is Event.Stop -> handleStop(sideEffects)
                    else -> state
                }
            }

            is State.Connecting -> {
                when (event) {
                    is Event.Send -> {
                        state.copy(pendingSendables = state.pendingSendables + event.sendable)
                    }
                    is Event.Cancel -> {
                        state.copy(pendingSendables = state.pendingSendables - event.sendable)
                    }
                    is Event.ConnectionError -> {
                        val newAttempt = state.attempt + 1

                        sideEffects += SideEffect.ScheduleReconnect(newAttempt)
                        State.WaitingForReconnect(state.url, newAttempt, state.pendingSendables)
                    }
                    is Event.Connected -> {
                        if (state.pendingSendables.isNotEmpty()) {
                            sideEffects += SideEffect.SendSendables(state.pendingSendables)
                        }

                        State.Connected(
                            url = state.url,
                            toResendOnReconnect = state.pendingSendables.filterByDeliveryType(
                                DeliveryType.ON_RECONNECT
                            ),
                            waitingForResponse = state.pendingSendables,
                            subscriptions = emptySet()
                        )
                    }
                    is Event.Stop -> handleStop(sideEffects)
                    else -> state
                }
            }

            is State.Connected -> {
                when (event) {
                    is Event.Send -> {
                        val sendable = event.sendable

                        sideEffects += SideEffect.SendSendables(setOf(sendable))

                        val toResendOnReconnect =
                            if (sendable.deliveryType == DeliveryType.ON_RECONNECT) {
                                state.toResendOnReconnect + sendable
                            } else {
                                state.toResendOnReconnect
                            }

                        state.copy(
                            toResendOnReconnect = toResendOnReconnect,
                            waitingForResponse = state.waitingForResponse + sendable
                        )
                    }

                    is Event.Cancel -> {
                        val sendable = event.sendable

                        val subscription =
                            findSubscriptionByInitiator(state.subscriptions, sendable)

                        val subscriptions = subscription?.let {
                            sideEffects += SideEffect.Unsubscribe(it)

                            state.subscriptions - it
                        } ?: state.subscriptions

                        state.copy(
                            toResendOnReconnect = state.toResendOnReconnect - sendable,
                            waitingForResponse = state.waitingForResponse - sendable,
                            subscriptions = subscriptions
                        )
                    }

                    is Event.SendableResponse -> {
                        val sendable = findSendableById(state.waitingForResponse, event.response.id)

                        if (sendable != null) {
                            sideEffects += SideEffect.ResponseToSendable(sendable, event.response)

                            state.copy(waitingForResponse = state.waitingForResponse - sendable)
                        } else {
                            state
                        }
                    }

                    is Event.Subscribed -> {
                        state.copy(subscriptions = state.subscriptions + event.subscription)
                    }

                    is Event.SubscriptionResponse -> {
                        val subscription = findSubscriptionById(
                            state.subscriptions,
                            event.response.subscriptionId
                        )

                        if (subscription != null) {
                            sideEffects += SideEffect.RespondToSubscription(
                                subscription,
                                event.response
                            )
                        }

                        state
                    }

                    is Event.ConnectionError -> {
                        val toReportError = state.waitingForResponse.filterByDeliveryType(DeliveryType.AT_MOST_ONCE)

                        if (toReportError.isNotEmpty()) {
                            sideEffects += SideEffect.RespondSendablesError(
                                toReportError,
                                event.throwable
                            )
                        }

                        val toResend = state.waitingForResponse - toReportError + state.toResendOnReconnect

                        sideEffects += SideEffect.ScheduleReconnect(attempt = 0)
                        State.WaitingForReconnect(url = state.url, pendingSendables = toResend)
                    }

                    is Event.Stop -> handleStop(sideEffects)
                    else -> state
                }
            }

            is State.Disconnected -> {
                when (event) {
                    is Event.Start -> {
                        sideEffects += SideEffect.Connect(event.url)

                        State.Connecting(event.url)
                    }
                    else -> state
                }
            }
        }

        return Transition(newState, sideEffects)
    }

    private fun findSubscriptionById(subscriptions: Set<Subscription>, id: String) =
        subscriptions.find { it.id == id }

    private fun findSendableById(sendables: Set<Sendable>, id: Int) = sendables.find { it.id == id }

    private fun findSubscriptionByInitiator(subscriptions: Set<Subscription>, initiator: Sendable) =
        subscriptions.find { it.initiatorId == initiator.id }

    private fun handleStop(sideEffects: MutableList<SideEffect>): State {
        sideEffects += SideEffect.Disconnect

        return State.Disconnected
    }

    private fun Set<Sendable>.filterByDeliveryType(deliveryType: DeliveryType): Set<Sendable> =
        filterTo(mutableSetOf()) {
            it.deliveryType == deliveryType
        }
}