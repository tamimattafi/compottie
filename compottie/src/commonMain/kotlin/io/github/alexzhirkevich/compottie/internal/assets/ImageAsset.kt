package io.github.alexzhirkevich.compottie.internal.assets

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.unit.IntSize
import io.github.alexzhirkevich.compottie.internal.helpers.BooleanInt
import io.github.alexzhirkevich.compottie.internal.platform.fromBytes
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Serializable
internal class ImageAsset(

    @SerialName("id")
    override val id: String,

    @SerialName("p")
    override val fileName: String,

    @SerialName("u")
    override val path: String ="",


    @SerialName("sid")
    override val slotId: String? = null,

    @SerialName("nm")
    val name: String? = null,

    @SerialName("e")
    override val embedded: BooleanInt = BooleanInt.No,

    @SerialName("w")
    private val w: Int? = null,

    @SerialName("h")
    private val h: Int? = null,
) : LottieFileAsset {

    val width: Int get() = w ?: bitmap?.width ?: 0

    val height: Int get() = h ?: bitmap?.height ?: 0

    @OptIn(ExperimentalEncodingApi::class)
    @Transient
    var bitmap: ImageBitmap? = fileName
        .takeIf(String::isBase64Data::get)
        ?.substringAfter("base64,")
        ?.trim()
        ?.let {
            ImageBitmap.fromBytes(Base64.decode(it))
        }?.let(::transformBitmap)
        private set

    fun setBitmap(bitmap: ImageBitmap) {
        this.bitmap = transformBitmap(bitmap)
    }

    private fun transformBitmap(bitmap: ImageBitmap) : ImageBitmap {
        return if (w != null && w != bitmap.width || h != null && h != bitmap.width) {
            bitmap.resize(w ?: bitmap.width, h ?: bitmap.height)
        } else bitmap
    }
}

private val String.isBase64Data : Boolean get() =
    (startsWith("data:") && indexOf("base64,") > 0)


private val emptyPaint = Paint()
internal fun ImageBitmap.resize(w : Int, h : Int) : ImageBitmap {
    val bitmap = ImageBitmap(w, h)

    Canvas(bitmap).apply {
        drawImageRect(
            image = this@resize,
            dstSize = IntSize(w, h),
            paint = emptyPaint
        )
    }

    return bitmap
}