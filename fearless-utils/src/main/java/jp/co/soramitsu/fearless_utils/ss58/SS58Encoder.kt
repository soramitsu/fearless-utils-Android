package jp.co.soramitsu.fearless_utils.ss58

import jp.co.soramitsu.fearless_utils.encrypt.Base58
import jp.co.soramitsu.fearless_utils.encrypt.json.copyBytes
import jp.co.soramitsu.fearless_utils.exceptions.AddressFormatException
import jp.co.soramitsu.fearless_utils.hash.Hasher.blake2b256
import jp.co.soramitsu.fearless_utils.hash.Hasher.blake2b512

class SS58Encoder {

    companion object {

        private val PREFIX = "SS58PRE".toByteArray(Charsets.UTF_8)
        private const val ADDRESS_TYPE_SIZE = 1
        private const val PREFIX_SIZE = 2
        private const val PUBLIC_KEY_SIZE = 32
    }

    private val base58 = Base58()

    fun encode(publicKey: ByteArray, addressByte: Byte): String {
        val normalizedKey = if (publicKey.size > 32) {
            publicKey.blake2b256()
        } else {
            publicKey
        }

        val addressTypeByteArray = byteArrayOf(addressByte)

        val hash = (PREFIX + addressTypeByteArray + normalizedKey).blake2b512()
        val checksum = hash.copyOfRange(0, PREFIX_SIZE)

        val resultByteArray = addressTypeByteArray + normalizedKey + checksum

        return base58.encode(resultByteArray)
    }

    fun decode(ss58String: String): ByteArray {
        val decodedByteArray = base58.decode(ss58String)

        return decodedByteArray.copyBytes(ADDRESS_TYPE_SIZE, PUBLIC_KEY_SIZE)
    }

    @Throws(AddressFormatException::class)
    fun extractAddressByte(address: String): Byte {
        val decodedByteArray = base58.decode(address)

        return decodedByteArray.first()
    }

    fun extractAddressByteOrNull(address: String): Byte? = try {
        extractAddressByte(address)
    } catch (e: AddressFormatException) {
        null
    }
}