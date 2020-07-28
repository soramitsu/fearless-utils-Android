package jp.co.soramitsu.fearless_utils.encrypt

import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.crypto.ed25519.EdDSASecurityProvider
import jp.co.soramitsu.crypto.ed25519.spec.EdDSANamedCurveTable
import jp.co.soramitsu.crypto.ed25519.spec.EdDSAPrivateKeySpec
import jp.co.soramitsu.fearless_utils.exceptions.JunctionTypeException
import jp.co.soramitsu.fearless_utils.junction.JunctionDecoder
import jp.co.soramitsu.fearless_utils.junction.JunctionType
import org.bouncycastle.jcajce.provider.digest.Blake2b
import org.spongycastle.util.encoders.Hex
import org.web3j.crypto.Sign
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.security.KeyFactory
import java.security.Security

class KeypairFactory {

    init {
        Security.addProvider(EdDSASecurityProvider())
    }

    private val junctionDecoder = JunctionDecoder()

    fun generate(encryptionType: EncryptionType, seed: ByteArray, derivationPath: String): Keypair {
        var previousKeypair = when (encryptionType) {
            EncryptionType.SR25519 -> deriveSr25519MasterKeypair(seed)
            EncryptionType.ED25519 -> deriveEd25519MasterKeypair(seed)
            EncryptionType.ECDCA -> deriveECDCAMasterKeypair(seed)
        }

        if (derivationPath.isNotEmpty()) {
            val junctions = junctionDecoder.decodeDerivationPath(derivationPath)

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
                            val inputSeed = Blake2b.Blake2b256()
                                .digest(buf.toByteArray() + previousKeypair.privateKey + it.chaincode)
                            deriveEd25519MasterKeypair(inputSeed)
                        } else {
                            throw JunctionTypeException()
                        }
                    }
                    EncryptionType.ECDCA -> {
                        if (it.type == JunctionType.HARD) {
                            val buf = ByteArrayOutputStream()
                            ScaleCodecWriter(buf).writeString("Secp256k1HDKD")
                            val inputSeed = Blake2b.Blake2b256()
                                .digest(buf.toByteArray() + previousKeypair.privateKey + it.chaincode)
                            deriveECDCAMasterKeypair(inputSeed)
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

    private fun decodeSr25519Keypair(bytes: ByteArray): Keypair {
        val privateKey = bytes.copyOfRange(0, 32)
        val nonce = bytes.copyOfRange(32, 64)
        val publicKey = bytes.copyOfRange(64, bytes.size)
        return Keypair(privateKey, publicKey, nonce)
    }
}