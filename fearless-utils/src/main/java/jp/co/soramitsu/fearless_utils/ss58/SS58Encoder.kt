package jp.co.soramitsu.fearless_utils.ss58

import jp.co.soramitsu.fearless_utils.exceptions.AddressTypeException
import jp.co.soramitsu.fearless_utils.encrypt.Base58
import org.spongycastle.jcajce.provider.digest.Blake2b

class SS58Encoder {

    companion object {

        private val PREFIX = "SS58PRE".toByteArray(Charsets.UTF_8)
        private const val ADDRESS_TYPE_SIZE = 1
        private const val PREFIX_SIZE = 2
        private const val PUBLIC_KEY_SIZE = 32
    }

    private val base58 = Base58()

    fun encode(publicKey: ByteArray, networkType: AddressType): String {
        val pubKey = if (publicKey.size > 32) {
            Blake2b.Blake2b256().digest(publicKey)
        } else {
            publicKey
        }

        val addressTypeByteArray = byteArrayOf(networkType.addressByte)
        val blake2b = Blake2b.Blake2b512().digest(PREFIX + addressTypeByteArray + pubKey)

        val resultByteArray = addressTypeByteArray + pubKey + blake2b.copyOfRange(0, PREFIX_SIZE)

        return base58.encode(resultByteArray)
    }

    fun decode(ss58String: String, networkType: AddressType): ByteArray {
        val decodedByteArray = base58.decode(ss58String)

        if (decodedByteArray.first() != networkType.addressByte) {
            throw AddressTypeException()
        }

        return decodedByteArray.copyOfRange(ADDRESS_TYPE_SIZE, PUBLIC_KEY_SIZE + ADDRESS_TYPE_SIZE)
    }

    fun getNetworkType(address: String): AddressType {
        val decodedByteArray = base58.decode(address)

        return when (decodedByteArray.first()) {
            AddressType.KUSAMA.addressByte -> AddressType.KUSAMA
            AddressType.POLKADOT.addressByte -> AddressType.POLKADOT
            else -> AddressType.KUSAMA
        }
    }
}