package jp.co.soramitsu.fearless_utils.encrypt

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

fun ByteArray.hmacSHA256(secret: ByteArray): ByteArray {
    val chiper: Mac = Mac.getInstance("HmacSHA256")
    val secretKeySpec = SecretKeySpec(secret, "HmacSHA256")
    chiper.init(secretKeySpec)

    return chiper.doFinal(this)
}