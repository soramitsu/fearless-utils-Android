package jp.co.soramitsu.fearless_utils.runtime

import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.fromByteArray
import jp.co.soramitsu.fearless_utils.wsrpc.request.base.RpcRequest
import jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.RuntimeRequest
import java.math.BigInteger

class Runtime(
    val typeRegistry: TypeRegistry,
    val metadata: Metadata,
    val rpcCalls: Set<String>
) {
    fun isRpcCallExists(call: String) = call in rpcCalls

    class Metadata(val modules: Map<String, Module>) {

        inner class Module(val name: String, val services: Map<String, Service>) {

            inner class Service(val name: String) {
                fun storageKey(string: String) = "STUB"
            }
        }
    }
}

interface Binding<T, V> {
    class IncompatibleException : Exception()

    @Throws(IncompatibleException::class)
    fun formRequest(runtime: Runtime, params: T): RpcRequest

    @Throws(IncompatibleException::class)
    fun parseResult(runtime: Runtime, response: ByteArray): V
}

internal fun incompatible(): Nothing = throw Binding.IncompatibleException()

object BalanceBinding : Binding<String, BigInteger> {

    override fun formRequest(runtime: Runtime, address: String): RpcRequest {
        if (!runtime.isRpcCallExists("state_getStorage")) incompatible()

        val storageKey =
            runtime.metadata.modules["Account"]?.services?.get("AccountInfo")?.storageKey(address)
                ?: incompatible()

        return RuntimeRequest("state_getStorage", listOf(storageKey))
    }

    override fun parseResult(runtime: Runtime, response: ByteArray): BigInteger {
        val accountInfoType = runtime.typeRegistry.get<Struct>("AccountInfo") ?: incompatible()
        val decoded = accountInfoType.fromByteArray(response)
        val accountData = decoded.get<Struct.Instance>("AccountData") ?: incompatible()

        // if we make all of u8, u16, u32 decode to BigInteger, we can easily tolerate such field changes in runtime
        return accountData.get<BigInteger>("free") ?: incompatible()
    }
}
