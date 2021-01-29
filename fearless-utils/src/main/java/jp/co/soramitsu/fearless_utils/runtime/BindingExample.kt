package jp.co.soramitsu.fearless_utils.runtime

import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.fromByteArray
import jp.co.soramitsu.fearless_utils.runtime.metadata.StorageEntry
import jp.co.soramitsu.fearless_utils.runtime.metadata.StorageEntryType
import jp.co.soramitsu.fearless_utils.wsrpc.request.base.RpcRequest
import jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.RuntimeRequest
import java.math.BigInteger

interface Binding<T, V> {
    class IncompatibleException : Exception()

    @Throws(IncompatibleException::class)
    fun formRequest(runtime: RuntimeSnapshot, params: T): RpcRequest

    @Throws(IncompatibleException::class)
    fun parseResult(runtime: RuntimeSnapshot, response: ByteArray): V
}

internal fun incompatible(): Nothing = throw Binding.IncompatibleException()

object BalanceBinding : Binding<AccountId, BigInteger> {

    override fun formRequest(runtime: RuntimeSnapshot, accountId: AccountId): RpcRequest {
        val storageKey = storageEntry(runtime)?.storageKey(runtime, accountId) ?: incompatible()

        return RuntimeRequest("state_getStorage", listOf(storageKey))
    }

    override fun parseResult(runtime: RuntimeSnapshot, response: ByteArray): BigInteger {
        val storageEntryType = storageEntry(runtime)?.type as? StorageEntryType.Map ?: incompatible()
        val decoded = storageEntryType.value?.fromByteArray(runtime, response) as? Struct.Instance ?: incompatible()
        val accountData = decoded.get<Struct.Instance>("AccountData") ?: incompatible()

        // if we make all of u8, u16, u32 decode to BigInteger, we can easily tolerate such field changes in runtime
        return accountData.get<BigInteger>("free") ?: incompatible()
    }

    private fun storageEntry(runtime: RuntimeSnapshot): StorageEntry? {
        return runtime.metadata.modules["System"]?.storage?.get("Account")
    }
}
