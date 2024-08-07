package com.example.alguardianguyproject

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.Button
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import android.view.WindowManager

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

    private lateinit var startButton: Button
    private lateinit var stopButton: Button

    init {
        // Set transparent background with border
        setBackgroundColor(ResourcesCompat.getColor(resources, android.R.color.transparent, null))
        background = ResourcesCompat.getDrawable(resources, R.drawable.resizable_border, null)

        // Add buttons to the view
        addButtons(context)
    }

    private fun addButtons(context: Context) {
        startButton = Button(context).apply {
            text = "Start Recording"
            setOnClickListener {
                // Start recording logic
                (context as? MainActivity)?.startRecording()
            }
        }

        stopButton = Button(context).apply {
            text = "Stop Recording"
            setOnClickListener {
                // Stop recording logic
                (context as? MainActivity)?.stopRecording()
            }
        }

        // Add buttons to the FrameLayout
        addView(startButton, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
            marginStart = 16
            topMargin = 16
        })

        addView(stopButton, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
            marginStart = 16
            topMargin = 80
        })
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

                val params = layoutParams as WindowManager.LayoutParams

                if (isInResizeRange(x, y)) {
                    // Resize the view
                    params.width = (width + dx).toInt().coerceAtLeast(minWidth)
                    params.height = (height + dy).toInt().coerceAtLeast(minHeight)
                    (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).updateViewLayout(this@ResizableOverlayView, params)

                    // Resize buttons
                    resizeButtons()
                } else {
                    // Move the view
                    val newX = (params.x + dx).toInt().coerceIn(0, (context.resources.displayMetrics.widthPixels - params.width))
                    val newY = (params.y + dy).toInt().coerceIn(0, (context.resources.displayMetrics.heightPixels - params.height))

                    params.x = newX
                    params.y = newY
                    (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).updateViewLayout(this@ResizableOverlayView, params)
                }

                lastTouchX = x
                lastTouchY = y
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                activePointerId = INVALID_POINTER_ID
            }
        }
        return true
    }

    private fun isInResizeRange(x: Float, y: Float): Boolean {
        val threshold = 50
        return (x > width - threshold && y > height - threshold)
    }

    private fun resizeButtons() {
        // Ensure buttons are resized proportionally
        val buttonMargin = 16
        val buttonWidth = (width - buttonMargin * 2) / 2
        val buttonHeight = (height - buttonMargin * 3) / 2

        if (buttonWidth > 0 && buttonHeight > 0) {
            startButton.layoutParams = LayoutParams(buttonWidth, buttonHeight).apply {
                marginStart = buttonMargin
                topMargin = buttonMargin
            }

            stopButton.layoutParams = LayoutParams(buttonWidth, buttonHeight).apply {
                marginStart = buttonMargin
                topMargin = buttonMargin + buttonHeight + buttonMargin
            }

            startButton.requestLayout()
            stopButton.requestLayout()
        }
    }

    companion object {
        private const val INVALID_POINTER_ID = -1
    }
}
