package jp.co.soramitsu.fearless_utils.hash

import net.jpountz.xxhash.XXHash64
import java.nio.ByteBuffer
import java.nio.ByteOrder

class XXHash128(private val xxHash64: XXHash64) {
    fun hash(byteArray: ByteArray): ByteArray {
        val hash1 = xxHash64.hash(byteArray, 0, byteArray.size, 0)
        val hash2 = xxHash64.hash(byteArray, 0, byteArray.size, 1)

        val hashBytes = ByteBuffer.allocate(Long.SIZE_BYTES * 2)
        hashBytes.order(ByteOrder.LITTLE_ENDIAN)

        hashBytes.putLong(hash1)
        hashBytes.putLong(hash2)

        return hashBytes.array()
    }
}