package jp.co.soramitsu.fearless_utils.encrypt

import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.fearless_utils.encrypt.model.Keypair
import jp.co.soramitsu.fearless_utils.exceptions.JunctionTypeException
import jp.co.soramitsu.fearless_utils.junction.JunctionType
import jp.co.soramitsu.fearless_utils.junction.SubstrateJunctionDecoder
import net.i2p.crypto.eddsa.EdDSAKey
import net.i2p.crypto.eddsa.EdDSASecurityProvider
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec
import org.bouncycastle.jcajce.provider.digest.Blake2b
import org.spongycastle.util.encoders.Hex
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.security.KeyFactory
import java.security.Security

class KeypairFactory {

    companion object {
        const val ed25519PrivateKeyPrefix = "302e020100300506032b657004220420"
        const val ed25519PubKeyPrefix = "302a300506032b6570032100"
    }

    init {
        Security.addProvider(EdDSASecurityProvider())
        Security.addProvider(org.spongycastle.jce.provider.BouncyCastleProvider())
    }

    private val junctionDecoder = SubstrateJunctionDecoder()

    fun generate(encryptionType: EncryptionType, seed: ByteArray, derivationPath: String = ""): Keypair {
        var previousKeypair = when (encryptionType) {
            EncryptionType.SR25519 -> deriveSr25519MasterKeypair(seed)
            EncryptionType.ED25519 -> deriveEd25519MasterKeypair(seed)
            EncryptionType.ECDSA -> deriveECDSAMasterKeypair(seed)
        }

        if (derivationPath.isNotEmpty()) {
            val decodeResult = junctionDecoder.decode(derivationPath)
            var currentSeed = seed
            decodeResult.junctions.forEach {
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
            private.copyOfRange(ed25519PrivateKeyPrefix.length / 2, private.size),
            public.copyOfRange(ed25519PubKeyPrefix.length / 2, public.size)
        )
    }

    private fun deriveECDSAMasterKeypair(seed: ByteArray): Keypair {
        val privateKey = BigInteger(Hex.toHexString(seed), 16)
        val compressed = ECDSAUtils.compressedPublicKeyFromPrivate(privateKey)
        return Keypair(
            seed,
            compressed
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
}