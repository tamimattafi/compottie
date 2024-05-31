package io.github.alexzhirkevich.compottie.internal.shapes

import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Shader
import io.github.alexzhirkevich.compottie.internal.platform.GradientShader
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedValue
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.animation.GradientColors
import io.github.alexzhirkevich.compottie.internal.animation.GradientType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("gs")
internal class GradientStrokeShape(

    @SerialName("mn")
    override val matchName : String? = null,

    @SerialName("nm")
    override val name : String? = null,

    @SerialName("hd")
    override val hidden : Boolean = false,

    @SerialName("lc")
    override val lineCap : LineCap = LineCap.Round,

    @SerialName("lj")
    override val lineJoin : LineJoin = LineJoin.Round,

    @SerialName("ml")
    override val strokeMiter : Float = 0f,

    @SerialName("o")
    override val opacity : AnimatedValue,

    @SerialName("w")
    override val strokeWidth : AnimatedValue,

    @SerialName("s")
    val startPoint : AnimatedVector2,

    @SerialName("e")
    val endPoint : AnimatedVector2,

    /**
     * Gradient Highlight Length. Only if type is Radial
     * */
    @SerialName("h")
    val highlightLength : AnimatedValue? = null,

    /**
     * Highlight Angle. Only if type is Radial
     * */
    @SerialName("a")
    val highlightAngle : AnimatedValue? = null,

    @SerialName("g")
    val colors : GradientColors,

    @SerialName("t")
    val type : GradientType,
)  : BaseStrokeShape(), Shape {

    @Transient
    private val boundsRect = MutableRect(0f,0f,0f,0f)

    @Transient
    private val gradientCache = LinkedHashMap<Int, Shader>()

    override fun draw(canvas: Canvas, parentMatrix: Matrix, parentAlpha: Float, frame: Float) {

        getBounds(boundsRect, parentMatrix, false, frame)

        paint.shader = GradientShader(
            type = type,
            startPoint = startPoint,
            endPoint = endPoint,
            colors = colors,
            frame = frame,
            matrix = parentMatrix,
            cache = gradientCache
        )
        super.draw(canvas, parentMatrix, parentAlpha, frame)
    }
}

