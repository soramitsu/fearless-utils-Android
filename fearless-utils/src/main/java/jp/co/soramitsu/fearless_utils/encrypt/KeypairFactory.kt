package jp.co.soramitsu.fearless_utils.encrypt

import jp.co.soramitsu.crypto.ed25519.EdDSASecurityProvider
import jp.co.soramitsu.crypto.ed25519.spec.EdDSANamedCurveTable
import jp.co.soramitsu.crypto.ed25519.spec.EdDSAPrivateKeySpec
import org.spongycastle.util.encoders.Hex
import org.web3j.crypto.Sign
import java.math.BigInteger
import java.security.KeyFactory
import java.security.Security

class KeypairFactory {

    init {
        Security.addProvider(EdDSASecurityProvider())
    }

    fun generate(encryptionType: EncryptionType, seed: ByteArray, derivationPath: String): Keypair {
        return if (derivationPath.isEmpty() && seed.isNotEmpty()) {
            when (encryptionType) {
                EncryptionType.SR25519 -> deriveSr25519MasterKeypair(seed)
                EncryptionType.ED25519 -> deriveEd25519MasterKeypair(seed)
                EncryptionType.ECDCA -> deriveECDCAMasterKeypair(seed)
            }
        } else {
            deriveSr25519MasterKeypair(seed)
        }
    }

    private fun deriveSr25519MasterKeypair(seed: ByteArray): Keypair {
        val keyPair = Sr25519.keypairFromSeed(seed)
        val privateKey = keyPair.copyOfRange(0, 31)
        val publicKey = keyPair.copyOfRange(64, keyPair.size)
        return Keypair(privateKey, publicKey)
    }

    private fun deriveEd25519MasterKeypair(seed: ByteArray): Keypair {
        val keyFac = KeyFactory.getInstance("EdDSA/SHA3", "EdDSA")
        val spec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519)
        val privKeySpec = EdDSAPrivateKeySpec(seed, spec)
        val private = keyFac.generatePrivate(privKeySpec).encoded
        val public = keyFac.generatePublic(privKeySpec).encoded
        return Keypair(private, public)
    }

    private fun deriveECDCAMasterKeypair(seed: ByteArray): Keypair {
        val privateKey = BigInteger(Hex.toHexString(seed), 16)
        val publicKey = Sign.publicKeyFromPrivate(privateKey)
        return Keypair(seed, publicKey.toByteArray())
    }
}