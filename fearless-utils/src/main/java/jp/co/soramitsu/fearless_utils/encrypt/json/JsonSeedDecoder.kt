package jp.co.soramitsu.fearless_utils.encrypt.json

import com.google.gson.Gson
import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType
import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType.ECDSA
import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType.ED25519
import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType.SR25519
import jp.co.soramitsu.fearless_utils.encrypt.KeypairFactory
import jp.co.soramitsu.fearless_utils.encrypt.Sr25519
import jp.co.soramitsu.fearless_utils.encrypt.json.JsonSeedDecodingException.IncorrectPasswordException
import jp.co.soramitsu.fearless_utils.encrypt.json.JsonSeedDecodingException.InvalidJsonException
import jp.co.soramitsu.fearless_utils.encrypt.json.JsonSeedDecodingException.UnsupportedEncryptionTypeException
import jp.co.soramitsu.fearless_utils.encrypt.model.ImportAccountData
import jp.co.soramitsu.fearless_utils.encrypt.model.ImportAccountMeta
import jp.co.soramitsu.fearless_utils.encrypt.model.JsonAccountData
import jp.co.soramitsu.fearless_utils.encrypt.model.Keypair
import jp.co.soramitsu.fearless_utils.encrypt.xsalsa20poly1305.SecretBox
import jp.co.soramitsu.fearless_utils.ss58.SS58Encoder
import org.spongycastle.crypto.generators.SCrypt
import org.spongycastle.util.encoders.Base64

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
            val encryptionType =
                EncryptionType.fromString(
                    encryptionTypeRaw
                )

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
        if (jsonData.encoding.type.size < 2 && jsonData.encoding.type[0] != ENCODING_SCRYPT && jsonData.encoding.type[1] != ENCODING_SALSA) {
            throw InvalidJsonException()
        }

        val username = jsonData.meta.name
        val address = jsonData.address
        val networkType = sS58Encoder.getNetworkType(jsonData.address)

        val byteData = Base64.decode(jsonData.encoded)

        val salt = byteData.copyBytes(SALT_OFFSET, SALT_SIZE)
        val N = byteData.copyBytes(N_OFFSET, N_SIZE).asLittleEndianInt()
        val p = byteData.copyBytes(P_OFFSET, P_SIZE).asLittleEndianInt()
        val r = byteData.copyBytes(R_OFFSET, R_SIZE).asLittleEndianInt()

        val nonce = byteData.copyBytes(NONCE_OFFSET, NONCE_SIZE)
        val encryptedData = byteData.copyOfRange(DATA_OFFSET, byteData.size)

        val encryptionSecret = SCrypt.generate(password.toByteArray(), salt, N, r, p, SCRYPT_KEY_SIZE)

        val secret = SecretBox(encryptionSecret).open(nonce, encryptedData)

        val importData = try {
            when (jsonData.encoding.content[1]) {
                SR25519.rawName -> {
                    val privateKeyCompressed = secret.copyOfRange(16, 80)
                    val privateAndNonce =
                        Sr25519.fromEd25519Bytes(
                            privateKeyCompressed
                        )
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