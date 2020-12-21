package jp.co.soramitsu.fearless_utils.extensions

import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

fun <T> concurrentHashSet(): MutableSet<T> = Collections.newSetFromMap(ConcurrentHashMap<T, Boolean>())