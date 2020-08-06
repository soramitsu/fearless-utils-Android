package jp.co.soramitsu.fearless_utils_android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.gson.Gson
import jp.co.soramitsu.fearless_utils.encrypt.JsonSeedDecoder
import jp.co.soramitsu.fearless_utils.encrypt.Sr25519
import jp.co.soramitsu.fearless_utils.encrypt.xsalsa20poly1305.SecretBox
import org.spongycastle.crypto.generators.SCrypt
import org.spongycastle.util.encoders.Base64
import org.spongycastle.util.encoders.Hex
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val jsonSeedDecoder = JsonSeedDecoder(Gson())

        val json = "{\"address\":\"5E7AjAWbQGmSsMgSSrLrXA4MKuDP7FQ4hGjzPQPF9P87esB5\",\"encoded\":\"5T2r7h0iRKk/R7gMELd5GQZ3Xt3vcyVSj9IHmVy2wmkAgAAAAQAAAAgAAAC0bXnDSQiHcDF2kaMo+iCV/rxpbN7tHlpM2WW43KYTee9rlD8i9wtskgn5K70d2hdqiVredGaTApcOLrrTEo8wgd1+21h2xmRNBt/iNa0xCIGTmdhASSXwgyr/UoRfLXv2retf37KrcTOWAej95mPPTx256IKzCyUHvkY0j6BlxNWKopcy9dOnit0J7a7ztWX+X5BLXwuUG8TWHGaZ\",\"encoding\":{\"content\":[\"pkcs8\",\"sr25519\"],\"type\":[\"scrypt\",\"xsalsa20-poly1305\"],\"version\":\"3\"},\"meta\":{\"genesisHash\":\"0xe143f23803ac50e8f6f8e62695d1ce9e4e1d68aa36c1cd2cfd15340213f3423e\",\"name\":\"aaa\",\"tags\":[],\"whenCreated\":1596624553160}}"
        val seed = "e3f9584a9a6b1128d252b21307ac3d85ed385beaac8185be9a299fa727d2ff29"
        println(Hex.toHexString(Sr25519.keypairFromSeed(Hex.decode(seed))))

        val jsonDec = jsonSeedDecoder.decode(json)
        val byteData = Base64.decode(jsonDec.encoded)
//        <scrypt salt 32 bytes> + <scrypt N 4 bytes le number> + <scrypt p 4 bytes le number> + <scrypt r 4 bytes le number> + <salsa nonce 24 bytes> + <remaining encrypted data>
        val salt = byteData.copyOfRange(0, 32)
        val N = ByteBuffer.wrap(byteData.copyOfRange(32, 36)).order(ByteOrder.LITTLE_ENDIAN).int
        val p = ByteBuffer.wrap(byteData.copyOfRange(36, 40)).order(ByteOrder.LITTLE_ENDIAN).int
        val r = ByteBuffer.wrap(byteData.copyOfRange(40, 44)).order(ByteOrder.LITTLE_ENDIAN).int

        val nonce = byteData.copyOfRange(44, 68)
        println("NONECE: ${Hex.toHexString(nonce)}")
        val encrData = byteData.copyOfRange(68, byteData.size)
//        @param P     the bytes of the pass phrase.
//        * @param S     the salt to use for this invocation.
//        * @param N     CPU/Memory cost parameter. Must be larger than 1, a power of 2 and less than
//        *              <code>2^(128 * r / 8)</code>.
//        * @param r     the block size, must be &gt;= 1.
//        * @param p     Parallelization parameter. Must be a positive integer less than or equal to
//        *              <code>Integer.MAX_VALUE / (128 * r * 8)</code>.
//        * @param dkLen the length of the key to generate.
        val d = SCrypt.generate("karata".toByteArray(Charsets.UTF_8), salt, N, r, p, 64).copyOfRange(0, 32)
        val a = SecretBox(d).open(nonce, encrData)
        println(Hex.toHexString(byteArrayOf(48.toByte(), 83.toByte(), 2.toByte(), 1.toByte(), 1.toByte(), 48.toByte(), 5.toByte(), 6.toByte(), 3.toByte(), 43.toByte(), 101.toByte(), 112.toByte(), 4.toByte(), 34.toByte(), 4.toByte(), 32.toByte())))
        println(Hex.toHexString(byteArrayOf(161.toByte(), 35.toByte(), 3.toByte(), 33.toByte(), 0.toByte())))
        println(Hex.toHexString(a))
    }
}