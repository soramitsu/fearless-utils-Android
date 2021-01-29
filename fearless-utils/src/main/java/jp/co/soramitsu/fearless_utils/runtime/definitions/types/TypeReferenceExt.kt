package jp.co.soramitsu.fearless_utils.runtime.definitions.types

import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Alias

class CyclicAliasingException : Exception()

/**
 * @throws CyclicAliasingException
 */
fun TypeReference.resolveAliasing(): TypeReference {
    var aliased = this

    val alreadySeen = mutableSetOf(this)

    while (true) {
        val aliasedValue = aliased.value

        if (aliasedValue !is Alias) break

        aliased = aliasedValue.aliasedReference

        if (aliased in alreadySeen) { // self-aliased
            throw CyclicAliasingException()
        } else {
            alreadySeen += aliased
        }
    }

    return aliased
}

fun TypeReference.resolveAliasingOrNull() : TypeReference? {
    return runCatching { resolveAliasing() }.getOrNull()
}