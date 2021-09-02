package jp.co.soramitsu.fearless_utils.mnemonic

import org.spongycastle.crypto.digests.SHA512Digest
import org.spongycastle.crypto.generators.PKCS5S2ParametersGenerator
import org.spongycastle.crypto.params.KeyParameter
import java.text.Normalizer
import java.text.Normalizer.normalize

internal object SeedCreator {

    private const val SEED_PREFIX = "mnemonic"

    fun deriveSeed(
        entropy: ByteArray,
        seedLength: Int,
        passphrase: String? = null
    ): ByteArray {
        val generator = PKCS5S2ParametersGenerator(SHA512Digest())
        generator.init(
            entropy,
            normalize("${SEED_PREFIX}${passphrase.orEmpty()}", Normalizer.Form.NFKD).toByteArray(),
            2048
        )
        val key = generator.generateDerivedMacParameters(512) as KeyParameter
        return key.key.copyOfRange(0, seedLength)
    }
}