package jp.co.soramitsu.fearless_utils.common

import org.junit.Assert

inline fun <reified T : Throwable> assertThrows(block: () -> Unit) {
    var throwable: Throwable? = null

    try {
        block()
    } catch (t: Throwable) {
        throwable = t
    }

    Assert.assertNotNull("No error was thrown", throwable)
    Assert.assertTrue("${T::class} expected, but ${throwable!!::class} thrown", throwable is T)
}