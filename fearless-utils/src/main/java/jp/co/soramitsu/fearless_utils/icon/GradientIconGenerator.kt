package jp.co.soramitsu.fearless_utils.icon

import android.graphics.drawable.PictureDrawable
import com.caverock.androidsvg.SVG
import org.bouncycastle.jcajce.provider.digest.Blake2b
import kotlin.math.floor
import kotlin.math.sqrt

class GradientIconGenerator : IconGenerator() {

    private val birdPath = "M32 63.9995C49.6731 63.9995 64 49.6726 64 31.9995C64 14.3264 49.6731 -0.000488281 32 -0.000488281C14.3269 -0.000488281 0 14.3264 0 31.9995C0 49.6726 14.3269 63.9995 32 63.9995ZM4.35048 26.1646L7.79807 29.8C8.1771 30.1997 8.07049 30.7829 7.5658 31.0707C7.02728 31.3777 6.95489 32.0059 7.44277 32.3668C8.87282 33.4248 11.9286 35.4533 16.1214 36.9691C21.2779 38.8334 24.6992 39.6167 25.4544 39.7891C25.5684 39.8151 25.6782 39.8495 25.7837 39.894L26.9016 40.3655C27.1741 40.4805 27.4015 40.6578 27.5572 40.8766L30.8329 45.7448C31.4543 46.6185 32.9733 46.6185 33.5948 45.7448L36.8705 40.8766C37.0261 40.6578 37.2536 40.4805 37.526 40.3655L38.644 39.894C38.7495 39.8495 38.8592 39.8151 38.9733 39.7891C39.7284 39.6167 43.1497 38.8334 48.3063 36.9691C52.2899 35.5289 55.2471 33.6259 56.7588 32.5322C57.3865 32.0781 57.3318 31.2841 56.6831 30.8509C56.0496 30.4278 55.9727 29.6488 56.5146 29.1453L59.5848 26.2928C60.6042 25.3456 59.6071 23.8606 58.1037 24.0871L38.2454 27.0325C37.7731 27.1037 37.2883 26.9847 36.9379 26.7116C36.3148 26.2259 36.6691 25.4091 37.2687 24.9033L37.6033 24.621C38.2199 24.1008 38.4067 23.2648 37.7901 22.7446L33.4846 17.9977C32.8562 17.4676 31.8284 17.4676 31.2 17.9977L26.6375 22.7446C26.0209 23.2648 26.2078 24.1008 26.8244 24.621L27.159 24.9033C27.7586 25.4091 28.1129 26.2259 27.4897 26.7116C27.1394 26.9847 26.6545 27.1037 26.1823 27.0325L5.8959 24.0226C4.44144 23.8035 3.43466 25.1989 4.35048 26.1646Z"
    private val mainRadius = 32.0
    private val radius = 20

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

        stringBuilder.append("<defs>")
        stringBuilder.append("<filter id=\"blur\" x=\"0\" y=\"0\">")
        stringBuilder.append("<feGaussianBlur in=\"SourceGraphic\" stdDeviation=\"15\" />")
        stringBuilder.append("</filter>")
        stringBuilder.append("</defs>")

        stringBuilder.append("<clipPath id=\"clipBird\">")
        stringBuilder.append("<path fill-rule=\"evenodd\" clip-rule=\"evenodd\" d=\"$birdPath\" />")
        stringBuilder.append("</clipPath>")

        stringBuilder.append("<g clip-path=\"url(#clipBird)\">")

        circles.dropLast(1).forEachIndexed { index, circle ->
            stringBuilder.append("<defs>")
            stringBuilder.append("<radialGradient id=\"RadialGradient$index\">")
            stringBuilder.append("<stop offset=\"1%\" stop-color=\"${circle.colorString}\"/>")
            stringBuilder.append("<stop offset=\"100%\" stop-color=\"#ffffff00\"/>")
            stringBuilder.append("</radialGradient>")
            stringBuilder.append("</defs>")
            stringBuilder.append("<circle cx='${circle.x}' cy='${circle.y}' r='${circle.radius}' fill=\"url(#RadialGradient$index)\" />")
        }

        val lastCircle = circles.last()
        stringBuilder.append("<circle clip-path=\"url(#clipBird)\" cx='${lastCircle.x}' cy='${lastCircle.y}' r='${lastCircle.radius}' fill=\"#ffffff4d\" filter=\"url(#blur)\" />")
        stringBuilder.append("</g>")

        stringBuilder.append("</svg>")
        val str = stringBuilder.toString()
        val svg = SVG.getFromString(str)
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
        val idHash = Blake2b.Blake2b512().digest(id).mapIndexed { index, byte -> (byte + 256 - zeroHash[index]) % 256 }

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
        val colors = scheme.colors.mapIndexed { index, _ -> palette[scheme.colors[if (index < 18) (index + rot) % 18 else 18]] }
        var index = 0

        val mainCircleColor = String.format("#%06X", backgroundColor)

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
            Circle(mainRadius, mainRadius, colors[index], radius),
            Circle(mainRadius, mainRadius, mainCircleColor, mainRadius.toInt())
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
