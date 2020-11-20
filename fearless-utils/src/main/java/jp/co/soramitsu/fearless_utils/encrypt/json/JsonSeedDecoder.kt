package jp.co.soramitsu.fearless_utils.encrypt.json

import com.google.gson.Gson
import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType
import jp.co.soramitsu.fearless_utils.encrypt.KeypairFactory
import jp.co.soramitsu.fearless_utils.encrypt.Sr25519
import jp.co.soramitsu.fearless_utils.encrypt.json.JsonSeedDecodingException.IncorrectPasswordException
import jp.co.soramitsu.fearless_utils.encrypt.json.JsonSeedDecodingException.InvalidJsonException
import jp.co.soramitsu.fearless_utils.encrypt.model.ImportAccountData
import jp.co.soramitsu.fearless_utils.encrypt.model.ImportAccountMeta
import jp.co.soramitsu.fearless_utils.encrypt.model.JsonAccountData
import jp.co.soramitsu.fearless_utils.encrypt.model.Keypair
import jp.co.soramitsu.fearless_utils.encrypt.xsalsa20poly1305.SecretBox
import jp.co.soramitsu.fearless_utils.ss58.AddressType
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
            val networkType = getNetworkType(address, jsonData.meta.genesisHash)

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

        val networkType = getNetworkType(jsonData.address, jsonData.meta.genesisHash)

        val byteData = Base64.decode(jsonData.encoded)

        val salt = byteData.copyBytes(SALT_OFFSET, SALT_SIZE)
        val N = byteData.copyBytes(N_OFFSET, N_SIZE).asLittleEndianInt()
        val p = byteData.copyBytes(P_OFFSET, P_SIZE).asLittleEndianInt()
        val r = byteData.copyBytes(R_OFFSET, R_SIZE).asLittleEndianInt()

        val nonce = byteData.copyBytes(NONCE_OFFSET, NONCE_SIZE)
        val encryptedData = byteData.copyOfRange(DATA_OFFSET, byteData.size)

        val encryptionSecret = SCrypt.generate(password.toByteArray(), salt, N, r, p, SCRYPT_KEY_SIZE)

        val secret = SecretBox(encryptionSecret).open(nonce, encryptedData)

        validatePassword(secret)

        val cryptoType = EncryptionType.fromString(jsonData.encoding.content[1])

        val (keypair, seed) = when (cryptoType) {
            EncryptionType.SR25519 -> {
                val privateKeyCompressed = secret.copyOfRange(16, 80)
                val privateAndNonce =
                    Sr25519.fromEd25519Bytes(
                        privateKeyCompressed
                    )
                val publicKey = secret.copyOfRange(85, 117)

                val keypair = Keypair(
                    privateAndNonce.copyOfRange(0, 32),
                    publicKey,
                    privateAndNonce.copyOfRange(32, 64)
                )

                keypair to null
            }

            EncryptionType.ED25519 -> {
                val seed = secret.copyOfRange(16, 48)
                val keypair = keypairFactory.generate(cryptoType, seed)

                keypair to seed
            }

            EncryptionType.ECDSA -> {
                val seed = secret.copyOfRange(16, 48)
                val keys = keypairFactory.generate(cryptoType, seed)

                keys to seed
            }
        }

        val networkInformation = networkType?.let {
            ImportAccountData.NetworkSensitiveInformation(
                it, sS58Encoder.encode(keypair.publicKey, it)
            )
        }

        return ImportAccountData(
            keypair,
            cryptoType,
            username,
            networkInformation,
            seed
        )
    }

    private fun validatePassword(secret: ByteArray) {
        if (secret.isEmpty()) throw IncorrectPasswordException()
    }

    private fun getNetworkType(address: String, genesisHash: String?): AddressType? {
        return if (genesisHash != null) {
            AddressType.fromGenesis(genesisHash)
        } else {
            try {
                sS58Encoder.getNetworkType(address)
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun decodeJson(json: String): JsonAccountData {
        return try {
            gson.fromJson(json, JsonAccountData::class.java)
        } catch (exception: Exception) {
            throw InvalidJsonException()
        }
    }
}