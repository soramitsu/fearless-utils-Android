package jp.co.soramitsu.fearless_utils.icon

import android.graphics.drawable.PictureDrawable
import com.caverock.androidsvg.SVG
import org.spongycastle.jcajce.provider.digest.Blake2b
import kotlin.math.floor
import kotlin.math.sqrt

class MosaicIconGenerator : IconGenerator() {

    private val mainRadius = 32.0
    private val radius = 5

    override fun getSvgImage(
        id: ByteArray,
        sizeInPixels: Int,
        isAlternative: Boolean,
        backgroundColor: Int
    ): PictureDrawable {
        val circles = generateIconCircles(id, isAlternative, backgroundColor)

        val stringBuilder = StringBuilder()
        stringBuilder.append("<svg ")
        stringBuilder.append("viewBox='0 0 64 64' ")
        stringBuilder.append("width='$sizeInPixels' ")
        stringBuilder.append("height='$sizeInPixels' ")
        stringBuilder.append(">")
        circles.forEach {
            stringBuilder.append("<circle cx='${it.x}' cy='${it.y}' r='${it.radius}' fill='${it.colorString}' />")
        }
        stringBuilder.append("</svg>")
        val svg = SVG.getFromString(stringBuilder.toString())
        return PictureDrawable(svg.renderToPicture())
    }

    private fun generateIconCircles(
        id: ByteArray,
        isAlternative: Boolean = false,
        backgroundColor: Int
    ): List<Circle> {

        val r1 = if (isAlternative) {
            mainRadius / 8 * 5
        } else {
            mainRadius / 4 * 3
        }

        val rroot3o2 = r1 * sqrt(3.0) / 2
        val ro2 = r1 / 2
        val rroot3o4 = r1 * sqrt(3.0) / 4
        val ro4 = r1 / 4
        val r3o4 = r1 * 3 / 4

        val totalFreq = schemes.fold(0) { summ, schema -> summ + schema.frequency }

        val zeroHash = Blake2b.Blake2b512().digest(ByteArray(32) { 0 })
        val idHash = Blake2b.Blake2b512().digest(id)
            .mapIndexed { index, byte -> (byte + 256 - zeroHash[index]) % 256 }

        val sat = (floor(idHash[29].toDouble() * 70 / 256 + 26) % 80) + 30
        val d = floor((idHash[30].toDouble() + idHash[31].toDouble() * 256) % totalFreq)
        val scheme = findScheme(d.toInt())

        val palette = idHash.mapIndexed { index, byte ->
            val b = (byte + index % 28 * 58) % 256
            var resultColor = ""
            resultColor = when (b) {
                0 -> {
                    "#444"
                }
                255 -> {
                    "transparent"
                }
                else -> {
                    val h = floor(b.toDouble() % 64 * 360 / 64)
                    val array = arrayOf(53, 15, 35, 75)
                    val l = array[floor(b.toDouble() / 64).toInt()]
                    "hsl($h, $sat%, $l%)"
                }
            }
            resultColor
        }

        val rot = (idHash[28] % 6) * 3
        val colors =
            scheme.colors.mapIndexed { index, _ -> palette[scheme.colors[if (index < 18) (index + rot) % 18 else 18]] }
        var index = 0

        val mainCircleColor = "#${backgroundColor.toString(radix = 16)}"

        return mutableListOf(
            Circle(mainRadius, mainRadius, mainCircleColor, mainRadius.toInt()),
            Circle(mainRadius, mainRadius - r1, colors[index++], radius),
            Circle(mainRadius, mainRadius - ro2, colors[index++], radius),
            Circle(mainRadius - rroot3o4, mainRadius - r3o4, colors[index++], radius),
            Circle(mainRadius - rroot3o2, mainRadius - ro2, colors[index++], radius),
            Circle(mainRadius - rroot3o4, mainRadius - ro4, colors[index++], radius),
            Circle(mainRadius - rroot3o2, mainRadius, colors[index++], radius),
            Circle(mainRadius - rroot3o2, mainRadius + ro2, colors[index++], radius),
            Circle(mainRadius - rroot3o4, mainRadius + ro4, colors[index++], radius),
            Circle(mainRadius - rroot3o4, mainRadius + r3o4, colors[index++], radius),
            Circle(mainRadius, mainRadius + r1, colors[index++], radius),
            Circle(mainRadius, mainRadius + ro2, colors[index++], radius),
            Circle(mainRadius + rroot3o4, mainRadius + r3o4, colors[index++], radius),
            Circle(mainRadius + rroot3o2, mainRadius + ro2, colors[index++], radius),
            Circle(mainRadius + rroot3o4, mainRadius + ro4, colors[index++], radius),
            Circle(mainRadius + rroot3o2, mainRadius, colors[index++], radius),
            Circle(mainRadius + rroot3o2, mainRadius - ro2, colors[index++], radius),
            Circle(mainRadius + rroot3o4, mainRadius - ro4, colors[index++], radius),
            Circle(mainRadius + rroot3o4, mainRadius - r3o4, colors[index++], radius),
            Circle(mainRadius, mainRadius, colors[index], radius)
        )
    }

    private fun findScheme(d: Int): Scheme {
        var cum = 0
        schemes.forEach {
            val n = it.frequency
            cum += n
            if (d < cum) {
                return it
            }
        }
        throw RuntimeException()
    }
}
