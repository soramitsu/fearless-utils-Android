package jp.co.soramitsu.fearless_utils.encrypt

import jp.co.soramitsu.fearless_utils.encrypt.model.Keypair
import jp.co.soramitsu.fearless_utils.hash.Hasher
import net.i2p.crypto.eddsa.EdDSAEngine
import net.i2p.crypto.eddsa.EdDSAPrivateKey
import net.i2p.crypto.eddsa.EdDSAPublicKey
import net.i2p.crypto.eddsa.EdDSASecurityProvider
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable
import net.i2p.crypto.eddsa.spec.EdDSAParameterSpec
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec
import org.spongycastle.util.encoders.Hex
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Sign
import java.math.BigInteger
import java.security.Security
import java.security.Signature

object Signer {

    init {
        Security.addProvider(EdDSASecurityProvider())
        Security.addProvider(org.spongycastle.jce.provider.BouncyCastleProvider())
    }

    fun sign(
        encryptionType: EncryptionType,
        message: ByteArray,
        keypair: Keypair
    ): SignatureWrapper {
        return when (encryptionType) {
            EncryptionType.SR25519 -> signSr25519(message, keypair)
            EncryptionType.ED25519 -> signEd25519(message, keypair)
            EncryptionType.ECDSA -> signEcdsa(message, keypair)
        }
    }

    private fun signSr25519(message: ByteArray, keypair: Keypair): SignatureWrapper {
        require(keypair.nonce != null)

        val sign = Sr25519.sign(keypair.publicKey, keypair.privateKey + keypair.nonce, message)

        return SignatureWrapper.Other(signature = sign)
    }

    fun verifySr25519(message: ByteArray, signature: ByteArray, publicKeyBytes: ByteArray): Boolean {
        return Sr25519.verify(signature, message, publicKeyBytes)
    }

    private fun signEd25519(message: ByteArray, keypair: Keypair): SignatureWrapper {
        val spec: EdDSAParameterSpec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519)
        val sgr: Signature = Signature.getInstance(
            EdDSAEngine.SIGNATURE_ALGORITHM,
            EdDSASecurityProvider.PROVIDER_NAME
        )
        val privKeySpec = EdDSAPrivateKeySpec(keypair.privateKey, spec)
        val privateKey = EdDSAPrivateKey(privKeySpec)
        sgr.initSign(privateKey)
        sgr.update(message)
        return SignatureWrapper.Other(signature = sgr.sign())
    }

    fun verifyEd25519(
        message: ByteArray,
        signature: ByteArray,
        publicKeyBytes: ByteArray
    ): Boolean {
        val spec: EdDSAParameterSpec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519)
        val sgr: Signature = Signature.getInstance(
            EdDSAEngine.SIGNATURE_ALGORITHM,
            EdDSASecurityProvider.PROVIDER_NAME
        )

        val privKeySpec = EdDSAPublicKeySpec(publicKeyBytes, spec)
        val publicKey = EdDSAPublicKey(privKeySpec)
        sgr.initVerify(publicKey)
        sgr.update(message)

        return sgr.verify(signature)
    }

    private fun signEcdsa(message: ByteArray, keypair: Keypair): SignatureWrapper {
        val privateKey = BigInteger(Hex.toHexString(keypair.privateKey), 16)
        val publicKey = Sign.publicKeyFromPrivate(privateKey)

        val messageHash = Hasher.blake2b256.digest(message)

        val sign = Sign.signMessage(messageHash, ECKeyPair(privateKey, publicKey), false)

        return SignatureWrapper.Ecdsa(v = sign.v, r = sign.r, s = sign.s)
    }
}