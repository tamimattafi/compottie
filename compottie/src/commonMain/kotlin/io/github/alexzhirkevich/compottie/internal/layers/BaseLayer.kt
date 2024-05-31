package io.github.alexzhirkevich.compottie.internal.layers

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.util.fastForEachReversed
import io.github.alexzhirkevich.compottie.internal.content.Content
import io.github.alexzhirkevich.compottie.internal.content.DrawingContent
import io.github.alexzhirkevich.compottie.internal.services.LottieServiceLocator
import io.github.alexzhirkevich.compottie.internal.helpers.Transform
import io.github.alexzhirkevich.compottie.internal.utils.preConcat

internal abstract class BaseLayer() : DrawingContent {

    abstract val transform: Transform

    private val matrix = Matrix()

    protected val boundsMatrix = Matrix()

    private val rect = MutableRect(0f,0f,0f,0f)

    private var parentLayers : MutableList<BaseLayer>? = null

    private var parentLayer: BaseLayer? = null

    var serviceLocator : LottieServiceLocator? = null

    var density by mutableStateOf(1f)

    abstract fun drawLayer(
        canvas: Canvas,
        parentMatrix: Matrix,
        parentAlpha: Float,
        frame: Float,
    )

    override fun draw(canvas: Canvas, parentMatrix: Matrix, parentAlpha: Float, frame: Float) {
        buildParentLayerListIfNeeded()
        matrix.reset()

        matrix.setFrom(parentMatrix)
        parentLayers?.fastForEachReversed {
            matrix.preConcat(it.transform.matrix(frame))
        }

        var alpha = parentAlpha

        transform.opacity?.interpolated(frame)?.let {
            alpha = (alpha * (it / 100f)).coerceIn(0f, 1f)
        }

        matrix.preConcat(transform.matrix(frame))
        drawLayer(canvas, matrix, alpha, frame)
    }


    override fun getBounds(
        outBounds: MutableRect,
        parentMatrix: Matrix,
        applyParents: Boolean,
        frame: Float,
    ) {
        rect.set(0f, 0f, 0f, 0f)
        buildParentLayerListIfNeeded()
        boundsMatrix.setFrom(parentMatrix)

        if (applyParents) {
            parentLayers?.fastForEachReversed {
                boundsMatrix.preConcat(it.transform.matrix(frame))
            } ?: run {
                parentLayer?.transform?.matrix(frame)?.let {
                    boundsMatrix.preConcat(it)
                }
            }
        }

        boundsMatrix.preConcat(transform.matrix(frame))
    }

    override fun setContents(contentsBefore: List<Content>, contentsAfter: List<Content>) {
        //do nothing
    }

    fun setParentLayer(layer: BaseLayer){
        this.parentLayer = layer
    }

    private fun buildParentLayerListIfNeeded() {
        if (parentLayers != null) {
            return
        }
        if (parentLayer == null) {
            parentLayers = mutableListOf()
            return
        }

        parentLayers = mutableListOf()
        var layer: BaseLayer? = parentLayer
        while (layer != null) {
            parentLayers?.add(layer)
            layer = layer.parentLayer
        }
    }
}
