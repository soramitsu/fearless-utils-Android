package jp.co.soramitsu.fearless_utils.encrypt

import jp.co.soramitsu.crypto.ed25519.EdDSAPrivateKey
import jp.co.soramitsu.crypto.ed25519.spec.EdDSANamedCurveTable
import jp.co.soramitsu.crypto.ed25519.spec.EdDSAParameterSpec
import jp.co.soramitsu.crypto.ed25519.spec.EdDSAPrivateKeySpec
import jp.co.soramitsu.fearless_utils.encrypt.model.Keypair
import org.spongycastle.util.encoders.Hex
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Sign
import java.math.BigInteger
import java.security.Signature

class Signer {

    fun sign(encryptionType: EncryptionType, message: ByteArray, keypair: Keypair): SignatureWrapper {
        return when (encryptionType) {
            EncryptionType.SR25519 -> signSr25519(message, keypair)
            EncryptionType.ED25519 -> signEd25519(message, keypair)
            EncryptionType.ECDSA -> signEcdca(message, keypair)
        }
    }

    private fun signSr25519(message: ByteArray, keypair: Keypair): SignatureWrapper {
        val sign = Sr25519.sign(keypair.publicKey, keypair.privateKey, message)
        return SignatureWrapper(signature = sign)
    }

    private fun signEd25519(message: ByteArray, keypair: Keypair): SignatureWrapper {
        val spec: EdDSAParameterSpec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519)
        val sgr: Signature = Signature.getInstance("EdDSA/SHA3", "EdDSA")
        val privKeySpec = EdDSAPrivateKeySpec(keypair.privateKey, spec)
        val privateKey = EdDSAPrivateKey(privKeySpec)
        sgr.initSign(privateKey)
        sgr.update(message)
        return SignatureWrapper(signature = sgr.sign())
    }

    private fun signEcdca(message: ByteArray, keypair: Keypair): SignatureWrapper {
        val privateKey = BigInteger(Hex.toHexString(keypair.privateKey), 16)
        val publicKey = Sign.publicKeyFromPrivate(privateKey)
        val sign = Sign.signMessage(message, ECKeyPair(privateKey, publicKey))
        return SignatureWrapper(signature = sign.r + sign.s + sign.s)
    }
}