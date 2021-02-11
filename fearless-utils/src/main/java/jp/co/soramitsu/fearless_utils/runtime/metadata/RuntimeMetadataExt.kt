package jp.co.soramitsu.fearless_utils.runtime.metadata

/**
 * @throws NoSuchElementException if module was not found
 */
fun RuntimeMetadata.module(index: Int): Module =
    modules.values.first { it.index == index.toBigInteger() }

fun RuntimeMetadata.moduleOrNull(index: Int): Module? = nullOnException { module(index) }

/**
 * @throws NoSuchElementException ifN module was not found
 */
fun RuntimeMetadata.module(name: String) = moduleOrNull(name) ?: throw NoSuchElementException()

fun RuntimeMetadata.moduleOrNull(name: String): Module? = modules[name]

/**
 * @throws NoSuchElementException if call was not found
 */
fun Module.call(index: Int): Function = requireElementInMap(calls, index)

fun Module.callOrNull(index: Int): Function? = nullOnException { call(index) }

/**
 * @throws NoSuchElementException if call was not found
 */
fun Module.call(name: String): Function = callOrNull(name) ?: throw NoSuchElementException()

fun Module.callOrNull(name: String): Function? = calls?.get(name)

/**
 * @throws NoSuchElementException if calls was not found
 */
fun Module.event(index: Int): Event = requireElementInMap(events, index)

fun Module.eventOrNull(index: Int): Event? = nullOnException { event(index) }

/**
 * @throws NoSuchElementException if calls was not found
 */
fun Module.event(name: String): Event = eventOrNull(name) ?: throw NoSuchElementException()

fun Module.eventOrNull(name: String): Event? = events?.get(name)

private inline fun <T> nullOnException(block: () -> T): T? {
    return runCatching(block).getOrNull()
}

private fun <V> requireElementInMap(map: Map<String, V>?, index: Int): V {
    if (map == null) throw NoSuchElementException()

    return map.values.elementAtOrNull(index) ?: throw NoSuchElementException()
}