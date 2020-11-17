package jp.co.soramitsu.fearless_utils.extensions

import org.bouncycastle.util.encoders.Hex

private const val PREFIX = "0x"

fun toHexString(bytes: ByteArray, withPrefix: Boolean = false): String {
    val encoded = Hex.toHexString(bytes)

    return if (withPrefix) return PREFIX + encoded else encoded
}

fun fromHexString(content: String): ByteArray = Hex.decode(content.removePrefix(PREFIX))