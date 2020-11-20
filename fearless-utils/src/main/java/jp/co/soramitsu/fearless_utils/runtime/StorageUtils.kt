package jp.co.soramitsu.fearless_utils.runtime

import jp.co.soramitsu.fearless_utils.extensions.toHexString
import jp.co.soramitsu.fearless_utils.hash.Blake2b128
import jp.co.soramitsu.fearless_utils.hash.XXHash128
import jp.co.soramitsu.fearless_utils.hash.hashConcat
import net.jpountz.xxhash.XXHashFactory

typealias Hasher = (ByteArray) -> ByteArray

enum class IdentifierHasher(val hasher: Hasher) {
    Blake2b128concat(StorageUtils.blake2b128::hashConcat),
    TwoX64Concat(StorageUtils.xxHash64::hashConcat)
}

class Identifier(
    value: ByteArray,
    identifierHasher: IdentifierHasher
) {
    val id = identifierHasher.hasher(value)
}

object StorageUtils {
    val blake2b128 = Blake2b128()

    val xxHash64 = XXHashFactory.safeInstance().hash64()
    val xxHash128 = XXHash128(xxHash64)

    fun createStorageKey(
        service: Service<*>,
        identifier: Identifier?
    ): String {
        val moduleNameBytes = service.module.id.toByteArray()
        val serviceNameBytes = service.id.toByteArray()

        var keyBytes = moduleNameBytes.xxHash128() + serviceNameBytes.xxHash128()

        identifier?.let { keyBytes += it.id }

        return toHexString(keyBytes, withPrefix = true)
    }

    private fun ByteArray.xxHash128() = xxHash128.hash(this)
}