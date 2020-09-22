package jp.co.soramitsu.fearless_utils.wsrpc.request.runtime

import jp.co.soramitsu.fearless_utils.hash.Blake2b128
import jp.co.soramitsu.fearless_utils.hash.XXHash128
import net.jpountz.xxhash.XXHashFactory
import org.bouncycastle.util.encoders.Hex

public object StorageUtils {
    private val blake2b128 = Blake2b128()

    private val xxHash64 = XXHashFactory.fastestInstance().hash64()
    private val xxHash128 = XXHash128(xxHash64)

    public fun createStorageKey(moduleName: String, serviceName: String, identifier: ByteArray): String {
        val moduleNameBytes = moduleName.toByteArray()
        val serviceNameBytes = serviceName.toByteArray()

        val keyBytes = moduleNameBytes.xxHash128() + serviceNameBytes.xxHash128() + identifier.blake2bConcat()

        return toHexWithPrefix(keyBytes)
    }

    private fun ByteArray.xxHash128() = xxHash128.hash(this)

    private fun ByteArray.blake2bConcat(): ByteArray {
        val hashed = blake2b128.digest(this)

        return hashed + this
    }

    private fun toHexWithPrefix(bytes: ByteArray) = "0x${Hex.toHexString(bytes)}"
}