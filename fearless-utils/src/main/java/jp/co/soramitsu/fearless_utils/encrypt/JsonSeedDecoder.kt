package jp.co.soramitsu.fearless_utils.encrypt

import com.google.gson.Gson
import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType.ECDSA
import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType.ED25519
import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType.SR25519
import jp.co.soramitsu.fearless_utils.encrypt.JsonSeedDecodingException.IncorrectPasswordException
import jp.co.soramitsu.fearless_utils.encrypt.JsonSeedDecodingException.InvalidJsonException
import jp.co.soramitsu.fearless_utils.encrypt.JsonSeedDecodingException.UnsupportedEncryptionTypeException
import jp.co.soramitsu.fearless_utils.encrypt.model.ImportAccountData
import jp.co.soramitsu.fearless_utils.encrypt.model.ImportAccountMeta
import jp.co.soramitsu.fearless_utils.encrypt.model.JsonAccountData
import jp.co.soramitsu.fearless_utils.encrypt.model.Keypair
import jp.co.soramitsu.fearless_utils.encrypt.xsalsa20poly1305.SecretBox
import jp.co.soramitsu.fearless_utils.ss58.SS58Encoder
import org.spongycastle.crypto.generators.SCrypt
import org.spongycastle.util.encoders.Base64
import java.nio.ByteBuffer
import java.nio.ByteOrder

sealed class JsonSeedDecodingException : Exception() {
    class InvalidJsonException : JsonSeedDecodingException()
    class IncorrectPasswordException : JsonSeedDecodingException()
    class UnsupportedEncryptionTypeException : JsonSeedDecodingException()
}

class JsonSeedDecoder(
    private val gson: Gson,
    private val sS58Encoder: SS58Encoder,
    private val keypairFactory: KeypairFactory
) {
    fun extractImportMetaData(json: String): ImportAccountMeta {
        val jsonData = decodeJson(json)

        try {
            val address = jsonData.address
            val networkType = sS58Encoder.getNetworkType(address)

            val encryptionTypeRaw = jsonData.encoding.content[1]
            val encryptionType = EncryptionType.fromString(encryptionTypeRaw)

            val name = jsonData.meta.name

            return ImportAccountMeta(name, networkType, encryptionType)
        } catch (_: Exception) {
            throw InvalidJsonException()
        }
    }

    fun decode(json: String, password: String): ImportAccountData {
        val jsonData = decodeJson(json)

        try {
            return decode(jsonData, password)
        } catch (exception: IncorrectPasswordException) {
            throw exception
        } catch (_: Exception) {
            throw InvalidJsonException()
        }
    }

    private fun decode(jsonData: JsonAccountData, password: String): ImportAccountData {
        if (jsonData.encoding.type.size < 2 && jsonData.encoding.type[0] != "scrypt" && jsonData.encoding.type[1] != "xsalsa20-poly1305") {
            throw InvalidJsonException()
        }

        val username = jsonData.meta.name
        val address = jsonData.address
        val networkType = sS58Encoder.getNetworkType(jsonData.address)

        val byteData = Base64.decode(jsonData.encoded)

        val salt = byteData.copyOfRange(0, 32)
        val N = ByteBuffer.wrap(byteData.copyOfRange(32, 36)).order(ByteOrder.LITTLE_ENDIAN).int
        val p = ByteBuffer.wrap(byteData.copyOfRange(36, 40)).order(ByteOrder.LITTLE_ENDIAN).int
        val r = ByteBuffer.wrap(byteData.copyOfRange(40, 44)).order(ByteOrder.LITTLE_ENDIAN).int

        val nonce = byteData.copyOfRange(44, 68)
        val encrData = byteData.copyOfRange(68, byteData.size)

        val encryptionSecret =
            SCrypt.generate(password.toByteArray(Charsets.UTF_8), salt, N, r, p, 64)
                .copyOfRange(0, 32)
        val secret = SecretBox(encryptionSecret).open(nonce, encrData)

        val importData = try {
            when (jsonData.encoding.content[1]) {
                SR25519.rawName -> {
                    val privateKeyCompressed = secret.copyOfRange(16, 80)
                    val privateAndNonce = Sr25519.fromEd25519Bytes(privateKeyCompressed)
                    val publicKey = secret.copyOfRange(85, 117)

                    ImportAccountData(
                        Keypair(
                            privateAndNonce.copyOfRange(0, 32),
                            publicKey,
                            privateAndNonce.copyOfRange(32, 64)
                        ), SR25519, networkType, username, address
                    )
                }

                ED25519.rawName -> {
                    val seed = secret.copyOfRange(16, 48)
                    val keys = keypairFactory.generate(ED25519, seed)

                    ImportAccountData(keys, ED25519, networkType, username, address, seed)
                }

                ECDSA.rawName -> {
                    val seed = secret.copyOfRange(16, 48)
                    val keys = keypairFactory.generate(ECDSA, seed)

                    ImportAccountData(keys, ECDSA, networkType, username, address, seed)
                }

                else -> throw UnsupportedEncryptionTypeException()
            }
        } catch (_: Exception) {
            throw IncorrectPasswordException()
        }

        val extractedAddress = sS58Encoder.encode(importData.keypair.publicKey, networkType)

        if (extractedAddress != address) {
            throw IncorrectPasswordException()
        }

        return importData
    }

    private fun decodeJson(json: String): JsonAccountData {
        return try {
            gson.fromJson(json, JsonAccountData::class.java)
        } catch (exception: Exception) {
            throw InvalidJsonException()
        }
    }
}