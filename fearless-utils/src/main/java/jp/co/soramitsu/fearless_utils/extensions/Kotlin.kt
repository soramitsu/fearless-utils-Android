package jp.co.soramitsu.fearless_utils.extensions

import java.math.BigInteger
import java.nio.ByteOrder

inline fun <T, R> Iterable<T>.tryFindNonNull(transform: (T) -> R?): R? {
    for (item in this) {
        val transformed = transform(item)

        if (transformed != null) return transformed
    }

    return null
}

private const val UNSIGNED_SIGNUM = 1

fun ByteArray.fromUnsignedBytes(byteOrder: ByteOrder = ByteOrder.BIG_ENDIAN): BigInteger {
    // Big Integer accepts big endian numbers
    val ordered = if (byteOrder == ByteOrder.LITTLE_ENDIAN) reversedArray() else this

    return BigInteger(UNSIGNED_SIGNUM, ordered)
}