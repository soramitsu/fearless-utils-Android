package jp.co.soramitsu.fearless_utils.encrypt

import org.bouncycastle.jce.ECNamedCurveTable
import org.spongycastle.util.encoders.Hex
import java.math.BigInteger

object ECDSAUtils {

    fun compressPubKey(pubKey: BigInteger): String? {
        val pubKeyYPrefix = if (pubKey.testBit(0)) "03" else "02"
        val pubKeyHex = pubKey.toString(16)
        val pubKeyX = pubKeyHex.substring(0, 64)
        return pubKeyYPrefix + pubKeyX
    }

    fun decompressPubKey(compKey: ByteArray?): BigInteger {
        val spec = ECNamedCurveTable.getParameterSpec("secp256k1")
        val point = spec.curve.decodePoint(compKey)
        val x: ByteArray = point.xCoord.encoded
        val y: ByteArray = point.yCoord.encoded
        return Hex.toHexString(byteArrayOf(0x00) + x + y).toBigInteger(16)
    }
}