package com.example.alguardianguyproject

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat

class ResizableOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var activePointerId = INVALID_POINTER_ID

    private val minWidth = 200
    private val minHeight = 200

    init {
        // Add a border to make the resizable area visible
        background = ResourcesCompat.getDrawable(resources, R.drawable.resizable_border, null)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.x
                val y = event.y
                lastTouchX = x
                lastTouchY = y
                activePointerId = event.getPointerId(0)
            }
            MotionEvent.ACTION_MOVE -> {
                val pointerIndex = event.findPointerIndex(activePointerId)
                val x = event.getX(pointerIndex)
                val y = event.getY(pointerIndex)

                val dx = x - lastTouchX
                val dy = y - lastTouchY

                // Resize the view
                layoutParams.width = (width + dx).toInt().coerceAtLeast(minWidth)
                layoutParams.height = (height + dy).toInt().coerceAtLeast(minHeight)
                requestLayout()

                lastTouchX = x
                lastTouchY = y
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                activePointerId = INVALID_POINTER_ID
            }
        }
        return true
    }

    companion object {
        private const val INVALID_POINTER_ID = -1
    }
}


