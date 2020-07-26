package jp.co.soramitsu.fearless_utils.junction

import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import org.spongycastle.util.encoders.Hex
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class JunctionDecoder {

    companion object {
        private val HEX_ALPHABET = "0123456789abcdef"
    }

    fun decodeDerivationPath(path: String): List<Junction> {
        val chaincode = mutableListOf<Junction>()
        var slashCount = 0
        var currentType = JunctionType.NONE
        val currentJunctionBuilder = StringBuilder()

        path.forEach { c ->
            if (c == '/') {
                if (currentType != JunctionType.NONE) {
                    chaincode.add(
                        Junction(
                            currentType,
                            decodeJunction(currentJunctionBuilder.toString())
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

                    3 -> {
                        currentType = JunctionType.PASSWORD
                    }
                }

                currentJunctionBuilder.append(c)
            }
        }

        chaincode.add(
            Junction(
                currentType,
                decodeJunction(currentJunctionBuilder.toString())
            )
        )


        return chaincode
    }

    private fun decodeJunction(junction: String): ByteArray {
        junction.toDoubleOrNull()?.let {
            val bytes = ByteArray(8)
            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).putDouble(it)
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


    private fun isHexString(hexaDecimal: String): Boolean {
        for (char in hexaDecimal) {
            if (!HEX_ALPHABET.contains(char)) {
                return false
            }
        }
        return true
    }

}