package jp.co.soramitsu.fearless_utils_android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import jp.co.soramitsu.fearless_utils_android.icon.IconGenerator
import jp.co.soramitsu.fearless_utils_android.ss58.AddressType
import jp.co.soramitsu.fearless_utils_android.ss58.SS58Encoder
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val l = IconGenerator().getSvgImage(SS58Encoder().decode("5HGQVFcrz8fLD4oRvnFx5GhRn6ki5KkdwKHHaGmzFSDrhBhy", AddressType.WESTEND), 400)

        imageView.setImageDrawable(l)
    }
}