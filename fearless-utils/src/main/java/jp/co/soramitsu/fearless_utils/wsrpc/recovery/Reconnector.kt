package jp.co.soramitsu.fearless_utils.wsrpc.recovery

import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

private val DEFAULT_RECONNECT_STRATEGY =
    ExponentialReconnectStrategy(initialTime = 300L, base = 2.0)

typealias ReconnectAction = (url: String) -> Unit

class Reconnector(
    private val strategy: ReconnectStrategy = DEFAULT_RECONNECT_STRATEGY,
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
) {
    private var inProgress: Future<*>? = null

    fun scheduleConnect(currentAttempt: Int, url: String, reconnectAction: ReconnectAction) {
        reset()

        inProgress = executor.schedule(
            wrapReconnectAction(url, reconnectAction),
            strategy.getTimeForReconnect(currentAttempt),
            TimeUnit.MILLISECONDS
        )
    }

    fun connect(url: String, reconnectAction: ReconnectAction) {
        reset()

        reconnectAction(url)
    }

    fun reset() {
        inProgress?.cancel(true)
        inProgress = null
    }

    private fun wrapReconnectAction(url: String, how: ReconnectAction) = Runnable {
        inProgress = null

        how(url)
    }
}