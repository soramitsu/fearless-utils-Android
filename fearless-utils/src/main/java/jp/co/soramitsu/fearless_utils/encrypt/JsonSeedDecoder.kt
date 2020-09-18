package jp.co.soramitsu.fearless_utils.encrypt

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import jp.co.soramitsu.fearless_utils.encrypt.JsonSeedDecodingException.*
import jp.co.soramitsu.fearless_utils.encrypt.model.ImportAccountData
import jp.co.soramitsu.fearless_utils.encrypt.model.JsonAccountData
import jp.co.soramitsu.fearless_utils.encrypt.model.Keypair
import jp.co.soramitsu.fearless_utils.encrypt.xsalsa20poly1305.SecretBox
import jp.co.soramitsu.fearless_utils.ss58.SS58Encoder
import org.json.JSONException
import org.spongycastle.crypto.generators.SCrypt
import org.spongycastle.util.encoders.Base64
import java.lang.Exception
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

    fun decode(json: String, password: String): ImportAccountData {
        val jsonData = decodeJson(json)

        if (jsonData.encoding.type.size < 2 && jsonData.encoding.type[0] != "scrypt" && jsonData.encoding.type[1] != "xsalsa20-poly1305") {
            throw JSONException("")
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

        val encryptionSecret = SCrypt.generate(password.toByteArray(Charsets.UTF_8), salt, N, r, p, 64).copyOfRange(0, 32)
        val secret = SecretBox(encryptionSecret).open(nonce, encrData)
        val importData =  when (jsonData.encoding.content[1]) {
            "sr25519" -> {
                val privateKeyCompressed = secret.copyOfRange(16, 80)
                val privateAndNonce = Sr25519.fromEd25519Bytes(privateKeyCompressed)
                val publicKey = secret.copyOfRange(85, 117)
                ImportAccountData(Keypair(privateAndNonce.copyOfRange(0, 32), privateAndNonce.copyOfRange(32, 64), publicKey), EncryptionType.SR25519, networkType, username, address)
            }

            "ed25519" -> {
                val seed = secret.copyOfRange(16, 48)
                val keys = keypairFactory.generate(EncryptionType.ED25519, seed, "")
                ImportAccountData(keys, EncryptionType.ED25519, networkType, username, address)
            }

            "ecdsa" -> {
                val seed = secret.copyOfRange(16, 48)
                val keys = keypairFactory.generate(EncryptionType.ECDSA, seed, "")
                ImportAccountData(keys, EncryptionType.ECDSA, networkType, username, address)
            }
            else -> throw JSONException("")
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
        } catch (exception: JsonSyntaxException) {
            throw InvalidJsonException()
        }
    }
}