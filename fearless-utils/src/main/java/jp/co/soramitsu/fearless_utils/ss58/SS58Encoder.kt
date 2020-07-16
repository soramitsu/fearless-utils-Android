package jp.co.soramitsu.fearless_utils.ss58

import jp.co.soramitsu.fearless_utils.exceptions.AddressTypeException
import jp.co.soramitsu.fearless_utils.encrypt.Base58
import org.spongycastle.jcajce.provider.digest.Blake2b

class SS58Encoder(
    private val base58: Base58
) {

    companion object {

        private val PREFIX = "SS58PRE".toByteArray(Charsets.UTF_8)
        private const val ADDRESS_TYPE_SIZE = 1
        private const val PREFIX_SIZE = 2
        private const val PUBLIC_KEY_SIZE = 32
    }

    fun encode(publicKey: ByteArray, addressType: AddressType): String {
        val addressTypeByteArray = byteArrayOf(addressType.addressByte)
        val blake2b = Blake2b.Blake2b512().digest(PREFIX + addressTypeByteArray + publicKey)

        val resultByteArray = addressTypeByteArray + publicKey + blake2b.copyOfRange(0, PREFIX_SIZE)

        return base58.encode(resultByteArray)
    }

    fun decode(ss58String: String, addressType: AddressType): ByteArray {
        val decodedByteArray = base58.decode(ss58String)

        if (decodedByteArray.first() != addressType.addressByte) {
            throw AddressTypeException()
        }

        return decodedByteArray.copyOfRange(ADDRESS_TYPE_SIZE, PUBLIC_KEY_SIZE + ADDRESS_TYPE_SIZE)
    }
}