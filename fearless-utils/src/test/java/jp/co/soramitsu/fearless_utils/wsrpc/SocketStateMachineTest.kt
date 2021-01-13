package jp.co.soramitsu.fearless_utils.wsrpc

import jp.co.soramitsu.fearless_utils.wsrpc.state.SocketStateMachine.Event
import jp.co.soramitsu.fearless_utils.wsrpc.state.SocketStateMachine.SideEffect
import jp.co.soramitsu.fearless_utils.wsrpc.state.SocketStateMachine.State
import jp.co.soramitsu.fearless_utils.wsrpc.request.DeliveryType
import jp.co.soramitsu.fearless_utils.wsrpc.state.SocketStateMachine
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
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

val TEST_EXCEPTION = Exception()

val CONNECT_SIDE_EFFECT = SideEffect.Connect(URL)

@RunWith(MockitoJUnitRunner::class)
class SocketStateMachineTest {

    private val sideEffectLog = mutableListOf<SideEffect>()

    @Before
    fun before() {
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

        assertEquals(listOf(CONNECT_SIDE_EFFECT, SideEffect.SendSendables(sendables)), sideEffectLog)
    }

    @Test
    fun `should resend ON_RECONNECT request on each reconnect`() {
        var state = moveToConnected()

        val sendable = singleTestSendable(DeliveryType.ON_RECONNECT)
        val sendables = setOf(sendable)

        state = transition(state, Event.Send(sendable))

        val sendSideEffect = SideEffect.SendSendables(sendables)

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
                SideEffect.ScheduleReconnect(0, URL),
                CONNECT_SIDE_EFFECT, sendSideEffect
            ), sideEffectLog)
    }

    @Test
    fun `should delay request until ready to send`() {
        var state = moveToStart()

        val sendable = singleTestSendable(DeliveryType.AT_LEAST_ONCE)
        val sendables = setOf(sendable)

        state = transition(state, Event.Send(sendable))

        assertEquals(State.Connecting(attempt = 0, pendingSendables = sendables, url = URL), state)

        state = transition(state, Event.Connected)

        assertEquals(emptyConnectedState.copy(waitingForResponse = sendables), state)
        assertEquals(listOf(CONNECT_SIDE_EFFECT, SideEffect.SendSendables(sendables)), sideEffectLog)
    }

    @Test
    fun `should cancel not sent request`() {
        var state = moveToStart()

        val sendable = singleTestSendable(DeliveryType.AT_LEAST_ONCE)

        state = transition(state, Event.Send(sendable))
        state = transition(state, Event.Cancel(sendable))

        assertEquals(state, State.Connecting(attempt = 0, pendingSendables = emptySet(), url = URL))
        assertSideAffectLogWithConnect()
    }

    private fun moveToConnected(): State {
        val started = moveToStart()

        return transition(started, Event.Connected)
    }

    private fun moveToStart(): State {
        val initial = SocketStateMachine.initialState()

        return transition(initial, Event.Start(URL))
    }

    private fun assertSideAffectLogWithConnect() = assertEquals(listOf(CONNECT_SIDE_EFFECT), sideEffectLog)

    private fun transition(state: State, vararg events: Event): State {
        var tempState = state

        events.forEach {
            val (updatedState, sideEffects) = SocketStateMachine.transition(state, it)

            tempState = updatedState

            sideEffectLog += sideEffects
        }

        return tempState
    }
}