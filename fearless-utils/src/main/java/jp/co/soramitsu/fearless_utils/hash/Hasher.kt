package jp.co.soramitsu.fearless_utils.hash

import net.jpountz.xxhash.XXHashFactory
import org.bouncycastle.jcajce.provider.digest.Blake2b

object Hasher {
    val blake2b256 = Blake2b.Blake2b256()
    val blake2b128 = Blake2b128()

    val xxHash64 = XXHashFactory.safeInstance().hash64()
    val xxHash128 = XXHash128(xxHash64)

    fun ByteArray.xxHash128() = xxHash128.hash(this)
    fun ByteArray.xxHash64() = xxHash64.hash(this)

    fun ByteArray.blake2b256() = blake2b256.digest(this)
    fun ByteArray.blake2b128() = blake2b128.digest(this)
}