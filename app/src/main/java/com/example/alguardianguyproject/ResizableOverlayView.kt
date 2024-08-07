package com.example.alguardianguyproject

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.Button
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import android.view.WindowManager
import android.widget.LinearLayout

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

    private lateinit var buttonContainer: LinearLayout

    init {
        // Set transparent background with border
        setBackgroundColor(ResourcesCompat.getColor(resources, android.R.color.transparent, null))
        background = ResourcesCompat.getDrawable(resources, R.drawable.resizable_border, null)

        // Add buttons to the view
        addButtons(context)
    }

    private fun addButtons(context: Context) {
        buttonContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }

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

        // Add buttons to the button container
        buttonContainer.addView(startButton, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        buttonContainer.addView(stopButton, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

        // Add button container to the FrameLayout
        addView(buttonContainer, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            gravity = android.view.Gravity.BOTTOM
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
                } else {
                    // Move the view
                    val newX = (params.x + dx).toInt().coerceIn(0, (context.resources.displayMetrics.widthPixels - params.width))
                    val newY = (params.y + dy).toInt().coerceIn(0, (context.resources.displayMetrics.heightPixels - params.height))

                    params.x = newX
                    params.y = newY
                }

                (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).updateViewLayout(this@ResizableOverlayView, params)

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
        val threshold = 100 // Increase threshold for easier resizing
        return (x > width - threshold && y > height - threshold)
    }

    companion object {
        private const val INVALID_POINTER_ID = -1
    }
}
