package jp.co.soramitsu.fearless_utils.encrypt

import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.fearless_utils.encrypt.model.Keypair
import jp.co.soramitsu.fearless_utils.exceptions.JunctionTypeException
import jp.co.soramitsu.fearless_utils.junction.JunctionDecoder
import jp.co.soramitsu.fearless_utils.junction.JunctionType
import net.i2p.crypto.eddsa.EdDSAKey
import net.i2p.crypto.eddsa.EdDSASecurityProvider
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec
import org.bouncycastle.jcajce.provider.digest.Blake2b
import org.spongycastle.crypto.params.ECPublicKeyParameters
import org.spongycastle.jcajce.provider.asymmetric.util.ECUtil
import org.spongycastle.util.encoders.Hex
import org.web3j.crypto.Sign
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.Security
import java.security.spec.ECGenParameterSpec

class KeypairFactory {

    init {
        Security.addProvider(EdDSASecurityProvider())
        Security.addProvider(org.spongycastle.jce.provider.BouncyCastleProvider())
    }

    private val junctionDecoder = JunctionDecoder()

    fun generate(encryptionType: EncryptionType, seed: ByteArray, derivationPath: String): Keypair {
        var previousKeypair = when (encryptionType) {
            EncryptionType.SR25519 -> deriveSr25519MasterKeypair(seed)
            EncryptionType.ED25519 -> deriveEd25519MasterKeypair(seed)
            EncryptionType.ECDSA -> deriveECDSAMasterKeypair(seed)
        }


        if (derivationPath.isNotEmpty()) {
            val junctions = junctionDecoder.decodeDerivationPath(derivationPath)
            var currentSeed = seed
            junctions.forEach {
                previousKeypair = when (encryptionType) {
                    EncryptionType.SR25519 -> {
                        if (it.type == JunctionType.SOFT) {
                            deriveSr25519SoftKeypair(it.chaincode, previousKeypair)
                        } else {
                            deriveSr25519HardKeypair(it.chaincode, previousKeypair)
                        }
                    }
                    EncryptionType.ED25519 -> {
                        if (it.type == JunctionType.HARD) {
                            val buf = ByteArrayOutputStream()
                            ScaleCodecWriter(buf).writeString("Ed25519HDKD")
                            currentSeed = Blake2b.Blake2b256()
                                .digest(buf.toByteArray() + currentSeed + it.chaincode)
                            deriveEd25519MasterKeypair(currentSeed)
                        } else {
                            throw JunctionTypeException()
                        }
                    }
                    EncryptionType.ECDSA -> {
                        if (it.type == JunctionType.HARD) {
                            val buf = ByteArrayOutputStream()
                            ScaleCodecWriter(buf).writeString("Secp256k1HDKD")
                            currentSeed = Blake2b.Blake2b256()
                                .digest(buf.toByteArray() + currentSeed + it.chaincode)
                            deriveECDSAMasterKeypair(currentSeed)
                        } else {
                            throw JunctionTypeException()
                        }
                    }
                }
            }
        }

        return previousKeypair
    }

    private fun deriveSr25519MasterKeypair(seed: ByteArray): Keypair {
        val keypairBytes = Sr25519.keypairFromSeed(seed)
        return decodeSr25519Keypair(keypairBytes)
    }

    private fun deriveSr25519SoftKeypair(chaincode: ByteArray, previousKeypair: Keypair): Keypair {
        val keypair = previousKeypair.privateKey + previousKeypair.nonce!! + previousKeypair.publicKey
        val newKeypairbytes = Sr25519.deriveKeypairSoft(keypair, chaincode)
        return decodeSr25519Keypair(newKeypairbytes)
    }

    private fun deriveSr25519HardKeypair(chaincode: ByteArray, previousKeypair: Keypair): Keypair {
        val keypair = previousKeypair.privateKey + previousKeypair.nonce!! + previousKeypair.publicKey
        val newKeypairbytes = Sr25519.deriveKeypairHard(keypair, chaincode)
        return decodeSr25519Keypair(newKeypairbytes)
    }

    private fun deriveEd25519MasterKeypair(seed: ByteArray): Keypair {
        val keyFac = KeyFactory.getInstance(EdDSAKey.KEY_ALGORITHM, "EdDSA")
        val spec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519)
        val privKeySpec = EdDSAPrivateKeySpec(seed, spec)
        val private = keyFac.generatePrivate(privKeySpec).encoded
        val publicKeySpec = EdDSAPublicKeySpec(privKeySpec.a, spec)
        val public = keyFac.generatePublic(publicKeySpec).encoded
        return Keypair(
            private.copyOfRange(12, private.size),
            public.copyOfRange(12, public.size)
        )
    }

    private fun deriveECDSAMasterKeypair(seed: ByteArray): Keypair {
        val privateKey = BigInteger(Hex.toHexString(seed), 16)
        val publicKey = Sign.publicKeyFromPrivate(privateKey)
        val compressed = compressPubKey(publicKey)
        return Keypair(
            seed,
            Hex.decode(compressed)
        )
    }

    private fun decodeSr25519Keypair(bytes: ByteArray): Keypair {
        val privateKey = bytes.copyOfRange(0, 32)
        val nonce = bytes.copyOfRange(32, 64)
        val publicKey = bytes.copyOfRange(64, bytes.size)
        return Keypair(
            privateKey,
            publicKey,
            nonce
        )
    }

    private fun compressPubKey(pubKey: BigInteger): String? {
        val pubKeyYPrefix = if (pubKey.testBit(0)) "03" else "02"
        val pubKeyHex = pubKey.toString(16)
        val pubKeyX = pubKeyHex.substring(0, 64)
        return pubKeyYPrefix + pubKeyX
    }
}