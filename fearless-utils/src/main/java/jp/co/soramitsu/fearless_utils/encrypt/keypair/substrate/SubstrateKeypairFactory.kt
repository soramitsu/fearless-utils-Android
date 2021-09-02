package jp.co.soramitsu.fearless_utils.encrypt.keypair.substrate

import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType
import jp.co.soramitsu.fearless_utils.encrypt.keypair.Keypair
import jp.co.soramitsu.fearless_utils.encrypt.keypair.KeypairFactory
import jp.co.soramitsu.fearless_utils.encrypt.keypair.generate
import jp.co.soramitsu.fearless_utils.junction.SubstrateJunctionDecoder
import net.i2p.crypto.eddsa.EdDSASecurityProvider
import org.spongycastle.jce.provider.BouncyCastleProvider
import java.security.Security

object SubstrateKeypairFactory {

//    init {
//        Security.addProvider(EdDSASecurityProvider())
//        Security.addProvider(BouncyCastleProvider())
//    }

    private val junctionDecoder = SubstrateJunctionDecoder()

    private fun <K : Keypair> KeypairFactory<K>.generate(
        seed: ByteArray,
        derivationPath: String
    ): K = generate(junctionDecoder, seed, derivationPath)

    fun generate(
        encryptionType: EncryptionType,
        seed: ByteArray,
        derivationPath: String = ""
    ): Keypair = when (encryptionType) {
        EncryptionType.SR25519 -> Sr25519SubstrateKeypairFactory.generate(seed, derivationPath)
        EncryptionType.ED25519 -> Ed25519SubstrateKeypairFactory.generate(seed, derivationPath)
        EncryptionType.ECDSA -> ECDSASubstrateKeypairFactory.generate(seed, derivationPath)
    }
}