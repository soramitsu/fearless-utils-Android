package jp.co.soramitsu.fearless_utils.encrypt

import org.spongycastle.jce.ECNamedCurveTable
import org.spongycastle.util.encoders.Hex
import org.web3j.crypto.Sign
import java.math.BigInteger

object ECDSAUtils {

    fun compressedPublicKeyFromPrivate(privKey: BigInteger): ByteArray {
        val point = Sign.publicPointFromPrivate(privKey)
        return point.getEncoded(true)
    }

    fun decompressPubKey(compKey: ByteArray?): BigInteger {
        val spec = ECNamedCurveTable.getParameterSpec("secp256k1")
        val point = spec.curve.decodePoint(compKey)
        val x: ByteArray = point.xCoord.encoded
        val y: ByteArray = point.yCoord.encoded
        return Hex.toHexString(byteArrayOf(0x00) + x + y).toBigInteger(16)
    }
}