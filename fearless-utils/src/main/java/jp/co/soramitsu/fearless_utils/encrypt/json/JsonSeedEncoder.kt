package jp.co.soramitsu.fearless_utils.encrypt.json

import com.google.gson.Gson
import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType
import jp.co.soramitsu.fearless_utils.encrypt.Sr25519
import jp.co.soramitsu.fearless_utils.encrypt.model.JsonAccountData
import jp.co.soramitsu.fearless_utils.encrypt.model.Keypair
import jp.co.soramitsu.fearless_utils.encrypt.xsalsa20poly1305.SecretBox
import jp.co.soramitsu.fearless_utils.ss58.AddressType
import jp.co.soramitsu.fearless_utils.ss58.SS58Encoder
import org.spongycastle.crypto.generators.SCrypt
import org.spongycastle.util.encoders.Base64
import java.util.Random
import kotlin.math.pow

private val N = 2.0.pow(15.0).toInt()
private const val p = 1
private const val r = 8

@Suppress("EXPERIMENTAL_API_USAGE")
class JsonSeedEncoder(
    private val gson: Gson,
    private val sS58Encoder: SS58Encoder,
    private val random: Random
) {
    fun generate(
        keypair: Keypair,
        seed: ByteArray?,
        password: String,
        name: String,
        encryptionType: EncryptionType,
        addressType: AddressType
    ): String {
        val encoded = formEncodedField(keypair, seed, password, encryptionType)
        val address = sS58Encoder.encode(keypair.publicKey, addressType)

        val importData = JsonAccountData(
            encoded = encoded,
            address = address,
            encoding = JsonAccountData.Encoding.default(encryptionType),
            meta = JsonAccountData.Meta(
                name = name,
                createdOn = System.currentTimeMillis()
            )
        )

        return gson.toJson(importData)
    }

    private fun formEncodedField(
        keypair: Keypair,
        seed: ByteArray?,
        password: String,
        encryptionType: EncryptionType
    ): String {
        val pkcs8Bytes = when (encryptionType) {
            EncryptionType.SR25519 -> sr25519Secret(keypair)
            else -> otherSecret(seed!!, keypair.publicKey)
        }

        val salt = generateSalt()

        val encryptionKey = SCrypt.generate(password.toByteArray(), salt, N, r, p, SCRYPT_KEY_SIZE)

        val secretBox = SecretBox(encryptionKey)
        val nonce = secretBox.nonce(pkcs8Bytes)

        val secret = secretBox.seal(nonce, pkcs8Bytes)

        val encodedBytes = salt + N.asLittleEndianBytes() + p.asLittleEndianBytes() +
                r.asLittleEndianBytes() + nonce + secret

        return Base64.toBase64String(encodedBytes)
    }

    private fun otherSecret(seed: ByteArray, publicKey: ByteArray): ByteArray {
        return pkcs8Bytes(seed, publicKey)
    }

    private fun sr25519Secret(keypair: Keypair): ByteArray {
        requireNotNull(keypair.nonce)

        val ed25519BytesSecret = Sr25519.toEd25519Bytes(keypair.privateKey + keypair.nonce)

        return pkcs8Bytes(ed25519BytesSecret, keypair.publicKey)
    }

    private fun pkcs8Bytes(secret: ByteArray, publicKey: ByteArray): ByteArray {
        return PKCS8_HEADER + secret + PKCS8_DIVIDER + publicKey
    }

    private fun generateSalt(): ByteArray {
        val bytes = ByteArray(SALT_SIZE)

        random.nextBytes(bytes)

        return bytes
    }
}