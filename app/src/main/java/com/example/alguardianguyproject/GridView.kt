package com.example.alguardianguyproject

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.min

class GridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var gridPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.transparent_white)
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }
    private var gridSpacing = 50f // Adjust as needed
    private var offsetX = 0f
    private var offsetY = 0f
    private var overlayWidth = 0f
    private var overlayHeight = 0f

    private fun updateGridPaint() {
        val centerX = width / 2f + offsetX
        val centerY = height / 2f + offsetY + overlayHeight
        val radius = hypot(overlayWidth, overlayHeight)
        val startColor = ContextCompat.getColor(context, R.color.white)
        val endColor = ContextCompat.getColor(context, android.R.color.transparent)

        gridPaint.apply {
            shader = RadialGradient(
                centerX, centerY, radius,
                startColor, endColor, Shader.TileMode.CLAMP
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Draw vertical lines
        for (x in 0..(width * 2) step gridSpacing.toInt()) {
            canvas.drawLine(x.toFloat() - offsetX, 0f - offsetY - height, x.toFloat() - offsetX, height.toFloat() * 2, gridPaint)
        }

        // Draw horizontal lines
        for (y in 0..(height * 3) step gridSpacing.toInt()) {
            canvas.drawLine(0f - offsetX, y.toFloat() - offsetY - height, width.toFloat(), y.toFloat() - offsetY - height, gridPaint)
        }
    }

    // Optional: Add methods to customize grid properties
    fun setGridSpacing(spacing: Float) {
        gridSpacing = spacing
        invalidate() // Redraw the view with the new spacing
    }

    fun setGridColor(color: Int) {
        gridPaint.color = color
        invalidate()
    }

    fun addOffset(x: Float, y: Float) {
        offsetX += x
        offsetY += y
        updateGridPaint()
        invalidate()
    }

    fun addSizeOffset(x:Float, y: Float) {
        overlayWidth += x
        overlayHeight += y
        updateGridPaint()
        invalidate()
    }

//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        return false // Disable touch events for this view
//    }
//
//    override fun performClick(): Boolean {
//        return false
//    }
}