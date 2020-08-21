package jp.co.soramitsu.fearless_utils.bip39

import io.github.novacrypto.SecureCharBuffer
import io.github.novacrypto.bip39.MnemonicGenerator
import io.github.novacrypto.bip39.MnemonicValidator
import io.github.novacrypto.bip39.wordlists.English
import io.github.novacrypto.hashing.Sha256
import jp.co.soramitsu.fearless_utils.exceptions.Bip39Exception
import org.spongycastle.crypto.digests.SHA512Digest
import org.spongycastle.crypto.generators.PKCS5S2ParametersGenerator
import org.spongycastle.crypto.params.KeyParameter
import java.lang.Exception
import java.security.SecureRandom
import java.text.Normalizer
import java.text.Normalizer.normalize
import java.util.Arrays
import kotlin.math.floor

class Bip39 {

    companion object {
        private const val SEED_PREFIX = "mnemonic"
    }

    fun generateMnemonic(length: MnemonicLength): String {
        SecureCharBuffer().use { secure ->
            val entropy = ByteArray(length.byteLength)
            SecureRandom().nextBytes(entropy)
            MnemonicGenerator(EnglishWordList.INSTANCE)
                .createMnemonic(entropy, secure::append)
            Arrays.fill(entropy, 0.toByte())
            return secure.toStringAble().toString()
        }
    }

    fun generateEntropy(mnemonic: String): ByteArray {
        val words = normalize(mnemonic, Normalizer.Form.NFKD).split(' ')
        if (words.size % 3 != 0) {
            throw Bip39Exception()
        }

        val bits = words.map {
            val index = EnglishWordList.INSTANCE.getIndex(it)
            if (index == -1)
                throw Bip39Exception()
            lpad(index.toString(2), "0", 11)
        }
            .joinToString("")

        val dividerIndex = floor(bits.length.toDouble() / 33) * 32
        val entropyBits = bits.substring(0, dividerIndex.toInt())
        val checksumBits = bits.substring(dividerIndex.toInt())

        val entropyBytes = binaryStringToByteArray(entropyBits)

        if (entropyBytes.size < 16) {
            throw Bip39Exception()
        }

        if (entropyBytes.size > 32) {
            throw Bip39Exception()
        }

        if (entropyBytes.size % 4 != 0) {
            throw Bip39Exception()
        }

        val newChecksum = deriveChecksumBits(entropyBytes)

        if (newChecksum != checksumBits)
            throw Bip39Exception()

        return entropyBytes
    }

    private fun lpad(str: String, padString: String, length: Int): String {
        var string = str

        while (string.length < length) {
            string = padString + string
        }

        return string
    }

    private fun binaryStringToByteArray(str: String): ByteArray {
        val byteArray = mutableListOf<Byte>()
        val tempStringBuilder = StringBuilder()

        str.forEach { c ->
            tempStringBuilder.append(c)

            val tempString = tempStringBuilder.toString()

            if (tempString.length == 8) {
                val temp = tempStringBuilder.toString()

                if (temp.isNotEmpty()) {
                    val tempInteger = Integer.parseInt(tempString, 2)
                    byteArray.add(tempInteger.toByte())
                }

                tempStringBuilder.clear()
            }
        }

        return byteArray.toByteArray()
    }

    private fun bytesToBinaryString(bytes: ByteArray): String {
        return bytes.toUByteArray().joinToString("") { x -> lpad(x.toString(2), "0", 8); }
    }

    private fun deriveChecksumBits(entropy: ByteArray): String {
        val ent = entropy.size * 8
        val cs = ent / 32
        val hash = Sha256.sha256(entropy)

        return bytesToBinaryString(hash).substring(0, cs)
    }

    fun generateMnemonic(entropy: ByteArray): String {
        SecureCharBuffer().use { secure ->
            MnemonicGenerator(EnglishWordList.INSTANCE)
                .createMnemonic(entropy, secure::append)
            Arrays.fill(entropy, 0.toByte())
            return secure.toStringAble().toString()
        }
    }

    fun generateSeed(entropy: ByteArray, passphrase: String): ByteArray {
        val generator = PKCS5S2ParametersGenerator(SHA512Digest())
        generator.init(
            entropy,
            normalize("$SEED_PREFIX$passphrase", Normalizer.Form.NFKD).toByteArray(),
            2048
        )
        val key = generator.generateDerivedMacParameters(512) as KeyParameter
        return key.key.copyOfRange(0, 32)
    }

    fun isMnemonicValid(mnemonic: String): Boolean {
        return try {
            MnemonicValidator
                .ofWordList(English.INSTANCE)
                .validate(mnemonic)
            true
        } catch (e: Exception) {
            false
        }
    }
}