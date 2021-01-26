package jp.co.soramitsu.fearless_utils.extensions

import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

inline fun <T, R> Iterable<T>.tryFindNonNull(transform: (T) -> R?): R? {
    for(item in this) {
        val transformed = transform(item)

        if (transformed != null) return transformed
    }

    return null
}

fun <T> concurrentHashSet(): MutableSet<T> = Collections.newSetFromMap(ConcurrentHashMap<T, Boolean>())