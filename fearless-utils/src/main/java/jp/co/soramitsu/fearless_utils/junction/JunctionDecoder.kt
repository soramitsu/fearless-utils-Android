package jp.co.soramitsu.fearless_utils.junction

import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import org.spongycastle.jcajce.provider.digest.Blake2b
import org.spongycastle.util.encoders.Hex
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class JunctionDecoder {

    companion object {
        private const val HEX_ALPHABET = "0123456789abcdef"
    }

    fun getPassword(path: String): String {
        if (path.contains("///")) {
            return path.substring(path.indexOf("///")).substring(3)
        }

        return ""
    }

    fun decodeDerivationPath(derivationPath: String): List<Junction> {
        val path = if (derivationPath.contains("///")) {
            derivationPath.substring(0, derivationPath.indexOf("///"))
        } else {
            derivationPath
        }

        val chaincodes = mutableListOf<Junction>()
        var slashCount = 0
        var currentType = JunctionType.NONE
        val currentJunctionBuilder = StringBuilder()

        path.forEach { c ->
            if (c == '/') {
                if (currentType != JunctionType.NONE) {
                    chaincodes.add(
                        Junction(
                            currentType,
                            proccessBytes(decodeJunction(currentJunctionBuilder.toString()))
                        )
                    )
                    slashCount = 0
                    currentType = JunctionType.NONE
                    currentJunctionBuilder.clear()
                }
                slashCount++
            } else {
                when (slashCount) {
                    1 -> {
                        currentType = JunctionType.SOFT
                    }

                    2 -> {
                        currentType = JunctionType.HARD
                    }
                }

                currentJunctionBuilder.append(c)
            }
        }

        chaincodes.add(
            Junction(
                currentType,
                proccessBytes(decodeJunction(currentJunctionBuilder.toString()))
            )
        )

        return chaincodes
    }

    private fun decodeJunction(junction: String): ByteArray {
        junction.toLongOrNull()?.let {
            val bytes = ByteArray(8)
            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).putLong(it)
            return bytes
        }

        return if (isHexString(junction)) {
            Hex.decode(junction)
        } else {
            val buf = ByteArrayOutputStream()
            ScaleCodecWriter(buf).writeString(junction)
            buf.toByteArray()
        }
    }

    private fun proccessBytes(bytes: ByteArray): ByteArray {
        if (bytes.size < 32) {
            val newBytes = ByteArray(32)
            bytes.copyInto(newBytes)
            return newBytes
        }

        if (bytes.size > 32) {
            return Blake2b.Blake2b256().digest(bytes)
        }

        return bytes
    }

    private fun isHexString(hexaDecimal: String): Boolean {
        for (char in hexaDecimal) {
            if (!HEX_ALPHABET.contains(char)) {
                return false
            }
        }
        return true
    }
}