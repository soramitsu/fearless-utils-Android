package jp.co.soramitsu.fearless_utils.common

import jp.co.soramitsu.fearless_utils.wsrpc.logging.Logger
import org.junit.Assert

inline fun <reified T : Throwable> assertThrows(block: () -> Unit) : T {
    var throwable: Throwable? = null

    try {
        block()
    } catch (t: Throwable) {
        throwable = t
    }

    Assert.assertNotNull("No error was thrown", throwable)
    Assert.assertTrue("${T::class} expected, but ${throwable!!::class} thrown", throwable is T || throwable.cause is T)

    return throwable as? T ?: throwable.cause as T
}