package jp.co.soramitsu.fearless_utils.icon

import android.graphics.drawable.PictureDrawable

abstract class IconGenerator {
    protected val schemes = mutableListOf(
        Scheme("target", 1, arrayOf(0, 28, 0, 0, 28, 0, 0, 28, 0, 0, 28, 0, 0, 28, 0, 0, 28, 0, 1)),
        Scheme("cube", 20, arrayOf(0, 1, 3, 2, 4, 3, 0, 1, 3, 2, 4, 3, 0, 1, 3, 2, 4, 3, 5)),
        Scheme("quazar", 16, arrayOf(1, 2, 3, 1, 2, 4, 5, 5, 4, 1, 2, 3, 1, 2, 4, 5, 5, 4, 0)),
        Scheme("flower", 32, arrayOf(0, 1, 2, 0, 1, 2, 0, 1, 2, 0, 1, 2, 0, 1, 2, 0, 1, 2, 3)),
        Scheme("cyclic", 32, arrayOf(0, 1, 2, 3, 4, 5, 0, 1, 2, 3, 4, 5, 0, 1, 2, 3, 4, 5, 6)),
        Scheme("vmirror", 128, arrayOf(0, 1, 2, 3, 4, 5, 3, 4, 2, 0, 1, 6, 7, 8, 9, 7, 8, 6, 10)),
        Scheme("hmirror", 128, arrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 8, 6, 7, 5, 3, 4, 2, 11))
    )

    abstract fun getSvgImage(
        id: ByteArray,
        sizeInPixels: Int,
        isAlternative: Boolean = false,
        backgroundColor: Int = "eeeeee".toInt(radix = 16)
    ): PictureDrawable
}
