package jp.co.soramitsu.fearless_utils.encrypt.keypair.ethereum

import jp.co.soramitsu.fearless_utils.encrypt.keypair.Keypair
import jp.co.soramitsu.fearless_utils.encrypt.keypair.generate
import jp.co.soramitsu.fearless_utils.junction.BIP32JunctionDecoder
import net.i2p.crypto.eddsa.EdDSASecurityProvider
import org.spongycastle.jce.provider.BouncyCastleProvider
import java.security.Security

object EthereumKeypairFactory {

    init {
        //TODO dublicated inserts
        Security.addProvider(EdDSASecurityProvider())
        Security.addProvider(BouncyCastleProvider())
    }

    private val junctionDecoder = BIP32JunctionDecoder()

    fun generate(seed: ByteArray, derivationPath: String? = null): Keypair {
        return Bip32KeypairFactory.generate(junctionDecoder, seed, derivationPath)
    }
}