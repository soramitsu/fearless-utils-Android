package jp.co.soramitsu.fearless_utils.wsrpc.state

import jp.co.soramitsu.fearless_utils.wsrpc.request.DeliveryType
import jp.co.soramitsu.fearless_utils.wsrpc.response.RpcResponse
import jp.co.soramitsu.fearless_utils.wsrpc.state.SocketStateMachine.Event
import jp.co.soramitsu.fearless_utils.wsrpc.state.SocketStateMachine.SideEffect
import jp.co.soramitsu.fearless_utils.wsrpc.state.SocketStateMachine.SideEffect.Connect
import jp.co.soramitsu.fearless_utils.wsrpc.state.SocketStateMachine.SideEffect.Disconnect
import jp.co.soramitsu.fearless_utils.wsrpc.state.SocketStateMachine.SideEffect.RespondSendablesError
import jp.co.soramitsu.fearless_utils.wsrpc.state.SocketStateMachine.SideEffect.RespondToSubscription
import jp.co.soramitsu.fearless_utils.wsrpc.state.SocketStateMachine.SideEffect.ResponseToSendable
import jp.co.soramitsu.fearless_utils.wsrpc.state.SocketStateMachine.SideEffect.ScheduleReconnect
import jp.co.soramitsu.fearless_utils.wsrpc.state.SocketStateMachine.SideEffect.SendSendables
import jp.co.soramitsu.fearless_utils.wsrpc.state.SocketStateMachine.SideEffect.Unsubscribe
import jp.co.soramitsu.fearless_utils.wsrpc.state.SocketStateMachine.State
import jp.co.soramitsu.fearless_utils.wsrpc.subscription.response.SubscriptionChange
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

private const val URL = "TEST"

val emptyConnectedState = State.Connected(
    url = URL,
    toResendOnReconnect = emptySet(),
    waitingForResponse = emptySet(),
    subscriptions = emptySet()
)

class TestSendable(override val id: Int, override val deliveryType: DeliveryType) :
    SocketStateMachine.Sendable

class TestSubscription(override val id: String, override val initiatorId: Int) :
    SocketStateMachine.Subscription

fun singleTestSendable(deliveryType: DeliveryType) = TestSendable(0, deliveryType)

fun singleTestSubscription() = TestSubscription("0", 0)

private val TEST_RESPONSE = RpcResponse("", null, 0, null)

private val TEST_EXCEPTION = Exception()
private val CONNECT_SIDE_EFFECT = Connect(URL)

private val TEST_RESPONSE_EVENT = Event.SendableResponse(TEST_RESPONSE)

@RunWith(MockitoJUnitRunner::class)
class SocketStateMachineTest {

    private val sideEffectLog = mutableListOf<SideEffect>()

    @Mock
    private lateinit var testSubscriptionChange: SubscriptionChange

    @Before
    fun before() {
        `when`(testSubscriptionChange.subscriptionId).thenReturn("0")

        sideEffectLog.clear()
    }

    @Test
    fun `should be disconnected from beginning`() {
        val state = SocketStateMachine.initialState()

        assertEquals(state, State.Disconnected)
    }

    @Test
    fun `should start`() {
        val state = moveToStart()

        assertEquals(state, State.Connecting(attempt = 0, pendingSendables = emptySet(), url = URL))
        assertSideAffectLogWithConnect()
    }

    @Test
    fun `should connect`() {
        val state = moveToConnected()

        assertEquals(state, emptyConnectedState)
        assertSideAffectLogWithConnect()
    }

    @Test
    fun `should send AT_LEAST_ONCE request if connected`() {
        var state = moveToConnected()

        val sendable = singleTestSendable(DeliveryType.AT_LEAST_ONCE)
        val sendables = setOf(sendable)

        state = transition(state, Event.Send(sendable))

        assertEquals(state, emptyConnectedState.copy(waitingForResponse = sendables))

        assertEquals(listOf(CONNECT_SIDE_EFFECT, SendSendables(sendables)), sideEffectLog)
    }

    @Test
    fun `should resend ON_RECONNECT request on each reconnect`() {
        var state = moveToConnected()

        val sendable = singleTestSendable(DeliveryType.ON_RECONNECT)
        val sendables = setOf(sendable)

        state = transition(state, Event.Send(sendable))

        val sendSideEffect = SendSendables(sendables)

        val expectedConnectedState = emptyConnectedState.copy(
            toResendOnReconnect = sendables,
            waitingForResponse = sendables
        )

        assertEquals(expectedConnectedState, state)
        assertEquals(listOf(CONNECT_SIDE_EFFECT, sendSideEffect), sideEffectLog)

        state = transition(state, Event.ConnectionError(TEST_EXCEPTION))
        assertEquals(
            State.WaitingForReconnect(
                attempt = 0,
                pendingSendables = sendables,
                url = URL
            ), state
        )

        state = transition(state, Event.ReadyToReconnect)
        assertEquals(State.Connecting(attempt = 0, pendingSendables = sendables, url = URL), state)

        state = transition(state, Event.Connected)

        assertEquals(expectedConnectedState, state)

        assertEquals(
            listOf(
                CONNECT_SIDE_EFFECT, sendSideEffect,
                ScheduleReconnect(0),
                CONNECT_SIDE_EFFECT, sendSideEffect
            ), sideEffectLog
        )
    }

    @Test
    fun `should delay request when connecting`() {
        var state = moveToStart()

        val sendable = singleTestSendable(DeliveryType.AT_LEAST_ONCE)
        val sendables = setOf(sendable)

        state = transition(state, Event.Send(sendable))

        assertEquals(State.Connecting(attempt = 0, pendingSendables = sendables, url = URL), state)

        state = transition(state, Event.Connected)

        assertEquals(emptyConnectedState.copy(waitingForResponse = sendables), state)
        assertEquals(listOf(CONNECT_SIDE_EFFECT, SendSendables(sendables)), sideEffectLog)
    }

    @Test
    fun `should force reconnect after send request`() {
        var state = moveToWaitingForReconnect()

        val sendable = singleTestSendable(DeliveryType.AT_LEAST_ONCE)
        val sendables = setOf(sendable)

        state = transition(state, Event.Send(sendable))

        assertEquals(State.Connecting(attempt = 0, pendingSendables = sendables, url = URL), state)

        assertEquals(
            listOf(
                CONNECT_SIDE_EFFECT, ScheduleReconnect(attempt = 1),
                Connect(URL)
            ), sideEffectLog
        )
    }

    @Test
    fun `should cancel when waiting to reconnect`() {
        var state = moveToWaitingForReconnect()

        val sendable = singleTestSendable(DeliveryType.AT_LEAST_ONCE)

        state = transition(state, Event.Send(sendable))
        state = transition(state, Event.Cancel(sendable))

        assertEquals(State.Connecting(attempt = 0, pendingSendables = emptySet(), url = URL), state)

        assertEquals(
            listOf(
                CONNECT_SIDE_EFFECT, ScheduleReconnect(attempt = 1),
                Connect(URL)
            ), sideEffectLog
        )
    }

    @Test
    fun `should ignore invalid transition from waiting`() {
        val state = State.WaitingForReconnect(URL, pendingSendables = emptySet())

        val newState = transition(state, Event.ConnectionError(TEST_EXCEPTION))

        assertEquals(state, newState)
    }

    @Test
    fun `should ignore invalid transition from connecting`() {
        val state = State.Connecting(URL, pendingSendables = emptySet())

        val newState = transition(state, Event.Start(URL))

        assertEquals(state, newState)
    }

    @Test
    fun `should ignore invalid transition from connected`() {
        val newState = transition(emptyConnectedState, Event.Start(URL))

        assertEquals(emptyConnectedState, newState)
    }

    @Test
    fun `should ignore invalid transition from disconnected`() {
        val newState = transition(State.Disconnected, Event.ConnectionError(TEST_EXCEPTION))

        assertEquals(State.Disconnected, newState)
    }

    @Test
    fun `should cancel not sent request`() {
        var state = moveToStart()

        val sendable = singleTestSendable(DeliveryType.AT_LEAST_ONCE)

        state = transition(state, Event.Send(sendable))
        state = transition(state, Event.Cancel(sendable))

        assertEquals(State.Connecting(attempt = 0, pendingSendables = emptySet(), url = URL), state)
        assertSideAffectLogWithConnect()
    }

    @Test
    fun `should subscribe`() {
        var state = moveToConnected()

        val sendable = singleTestSendable(DeliveryType.ON_RECONNECT)
        val sendables = setOf(sendable)
        val subscription = singleTestSubscription()

        state = transition(
            state,
            Event.Send(sendable),
            TEST_RESPONSE_EVENT,
            Event.Subscribed(subscription)
        )

        val subscribedLog = listOf(
            CONNECT_SIDE_EFFECT, SendSendables(sendables),
            ResponseToSendable(sendable, TEST_RESPONSE)
        )

        val expectedState = emptyConnectedState.copy(
            toResendOnReconnect = sendables,
            subscriptions = setOf(subscription)
        )

        assertEquals(expectedState, state)
        assertEquals(subscribedLog, sideEffectLog)

        state = transition(state, Event.SubscriptionResponse(testSubscriptionChange))

        assertEquals(expectedState, state)
        assertEquals(
            subscribedLog + RespondToSubscription(subscription, testSubscriptionChange),
            sideEffectLog
        )
    }

    @Test
    fun `should unsubscribe`() {
        var state = moveToConnected()

        val sendable = singleTestSendable(DeliveryType.ON_RECONNECT)
        val sendables = setOf(sendable)
        val subscription = singleTestSubscription()

        state = transition(
            state,
            Event.Send(sendable),
            TEST_RESPONSE_EVENT,
            Event.Subscribed(subscription),
            Event.Cancel(sendable)
        )

        val expectedLog = listOf(
            CONNECT_SIDE_EFFECT, SendSendables(sendables),
            ResponseToSendable(sendable, TEST_RESPONSE), Unsubscribe(subscription)
        )

        assertEquals(emptyConnectedState, state)
        assertEquals(expectedLog, sideEffectLog)
    }

    @Test
    fun `should be able to stop when connected`() {
        var state = moveToConnected()

        state = transition(state, Event.Stop)

        assertEquals(State.Disconnected, state)
        assertEquals(sideEffectLog, listOf(CONNECT_SIDE_EFFECT, Disconnect))
    }

    @Test
    fun `should delay reconnect when error occurs`() {
        val state = moveToWaitingForReconnect()

        assertEquals(
            State.WaitingForReconnect(URL, attempt = 1, pendingSendables = emptySet()),
            state
        )
        assertSideAffectLogForReconnect()
    }

    @Test
    fun `should be able to stop when connecting`() {
        var state = moveToWaitingForReconnect()

        state = transition(state, Event.Stop)

        assertEquals(State.Disconnected, state)
        assertEquals(reconnectSideEffectLog() + Disconnect, sideEffectLog)
    }

    @Test
    fun `should be able to stop when waiting for reconnect`() {
        var state = moveToStart()

        state = transition(state, Event.Stop)

        assertEquals(State.Disconnected, state)
        assertEquals(sideEffectLog, listOf(CONNECT_SIDE_EFFECT, Disconnect))
    }

    @Test
    fun `should cancel while waiting for reconnect sent request`() {
        var state = moveToStart()

        val sendable = singleTestSendable(DeliveryType.AT_LEAST_ONCE)

        state = transition(state, Event.Send(sendable))

        state = transition(state, Event.ConnectionError(TEST_EXCEPTION))

        state = transition(state, Event.Cancel(sendable))

        assertEquals(
            State.WaitingForReconnect(
                attempt = 1,
                pendingSendables = emptySet(),
                url = URL
            ), state
        )
        assertSideAffectLogForReconnect()
    }

    @Test
    fun `should cancel request when connected`() {
        var state = moveToConnected()

        val sendable = singleTestSendable(DeliveryType.ON_RECONNECT)
        val sendables = setOf(sendable)

        state = transition(state, Event.Send(sendable), Event.Cancel(sendable))

        val expectedLog = listOf(CONNECT_SIDE_EFFECT, SendSendables(sendables))

        assertEquals(emptyConnectedState, state)
        assertEquals(expectedLog, sideEffectLog)
    }

    @Test
    fun `should ignore cancellation of unknown request`() {
        var state = moveToConnected()

        val sendable = singleTestSendable(DeliveryType.ON_RECONNECT)

        state = transition(state, Event.Cancel(sendable))

        val expectedLog = listOf(CONNECT_SIDE_EFFECT)

        assertEquals(emptyConnectedState, state)
        assertEquals(expectedLog, sideEffectLog)
    }

    @Test
    fun `should ignore response to unknown request`() {
        var state = moveToConnected()

        state = transition(state, Event.SendableResponse(TEST_RESPONSE))

        val expectedLog = listOf(CONNECT_SIDE_EFFECT)

        assertEquals(emptyConnectedState, state)
        assertEquals(expectedLog, sideEffectLog)
    }

    @Test
    fun `should report error to AT_MOST_ONCE request and forget about it on failure`() {
        var state = moveToConnected()

        val sendable = singleTestSendable(DeliveryType.AT_MOST_ONCE)
        val sendables = setOf(sendable)

        state = transition(state, Event.Send(sendable), Event.ConnectionError(TEST_EXCEPTION))

        val expectedLog = listOf(
            CONNECT_SIDE_EFFECT, SendSendables(sendables),
            RespondSendablesError(sendables, TEST_EXCEPTION), ScheduleReconnect(attempt = 0)
        )

        assertEquals(
            State.WaitingForReconnect(URL, attempt = 0, pendingSendables = emptySet()),
            state
        )
        assertEquals(expectedLog, sideEffectLog)
    }

    private fun moveToConnected(): State {
        val started = moveToStart()

        return transition(started, Event.Connected)
    }

    private fun moveToStart(): State {
        val initial = SocketStateMachine.initialState()

        return transition(initial, Event.Start(URL))
    }

    private fun moveToWaitingForReconnect(): State {
        val initial = SocketStateMachine.initialState()

        return transition(initial, Event.Start(URL), Event.ConnectionError(TEST_EXCEPTION))
    }

    private fun assertSideAffectLogWithConnect() =
        assertEquals(listOf(CONNECT_SIDE_EFFECT), sideEffectLog)

    private fun assertSideAffectLogForReconnect() =
        assertEquals(reconnectSideEffectLog(), sideEffectLog)

    private fun reconnectSideEffectLog() = listOf(CONNECT_SIDE_EFFECT, ScheduleReconnect(1))

    private fun transition(state: State, vararg events: Event): State {
        var tempState = state

        events.forEach {
            val (updatedState, sideEffects) = SocketStateMachine.transition(tempState, it)

            tempState = updatedState

            sideEffectLog += sideEffects
        }

        return tempState
    }
}