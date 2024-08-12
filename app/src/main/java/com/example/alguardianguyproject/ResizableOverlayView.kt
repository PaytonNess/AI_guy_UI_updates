package com.example.alguardianguyproject

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import kotlin.math.abs

class ResizableOverlayView @JvmOverloads constructor(
    context: Context,
    private val windowManager: WindowManager, // Add this
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var activePointerId = INVALID_POINTER_ID

    private val minWidth = 400
    private val minHeight = 400

    lateinit var startButton: Button

    private lateinit var buttonContainer: LinearLayout
    private lateinit var chatContainer: LinearLayout
    private lateinit var loadingContainer: LinearLayout
    private lateinit var closeContainer: LinearLayout

    private lateinit var chatRecyclerView: RecyclerView
    lateinit var messageInput: EditText
    lateinit var sendButton: Button
    lateinit var backButton: Button
    lateinit var closeButton: Button

    lateinit var recyclerView: RecyclerView
    lateinit var chatButton: Button

    init {

        // Set transparent background with border
        setBackgroundColor(ResourcesCompat.getColor(resources, android.R.color.transparent, null))
        background = ResourcesCompat.getDrawable(resources, R.drawable.resizable_border, null)
        minimumWidth = minWidth
        minimumHeight = minHeight
        // Add buttons to the view
        initChatComponents(context)
        initRecordComponents(context)
        initLoadingComponents(context)
        initCloseButton(context)

        addCloseButton()
        addRecordComponents()
    }

    private fun initChatComponents(context: Context) {
        chatContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        val inflater = LayoutInflater.from(context).inflate(R.layout.activity_chat, chatContainer, true)
        chatRecyclerView = inflater.findViewById(R.id.message_list)
        chatRecyclerView.layoutManager = LinearLayoutManager(context)
        messageInput = inflater.findViewById(R.id.message_input)
        sendButton = inflater.findViewById(R.id.send_button)
        backButton = inflater.findViewById(R.id.back_button)
    }

    private fun initCloseButton(context: Context) {
        closeContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        val inflater = LayoutInflater.from(context).inflate(R.layout.close_button, closeContainer, true)
        closeButton = inflater.findViewById(R.id.CloseButton)
    }

    private fun addCloseButton() {
        addView(closeContainer, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.TOP or Gravity.END
        })
        closeContainer.bringToFront()
    }

    fun addChatComponents() {
        addView(chatContainer, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply {
            gravity = Gravity.BOTTOM
        })
        background = ResourcesCompat.getDrawable(resources, R.drawable.resizable_border, null)
        closeContainer.bringToFront()
    }

    fun removeChatComponents() {
        removeView(chatContainer)
    }

    fun removeLoadingComponents() {
        removeView(loadingContainer)
    }

    fun removeRecordComponents() {
        removeView(buttonContainer)
    }

    private fun initLoadingComponents(context: Context) {
        loadingContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        val inflater = LayoutInflater.from(context).inflate(R.layout.activity_record, loadingContainer, true)
        recyclerView = inflater.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        chatButton = inflater.findViewById(R.id.chatButton)
    }

    fun addLoadingComponents() {
        addView(loadingContainer, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.BOTTOM
        })
        background = ResourcesCompat.getDrawable(resources, R.drawable.resizable_border_colored, null)
        closeContainer.bringToFront()
    }

    // Add a method to set the adapter for the RecyclerView
    fun setChatAdapter(adapter: RecyclerView.Adapter<*>) {
        chatRecyclerView.adapter = adapter
    }

    private fun initRecordComponents(context: Context) {
        buttonContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        val inflater1 = LayoutInflater.from(context).inflate(R.layout.send_button, buttonContainer, true)
        startButton = inflater1.findViewById(R.id.SendButton)
        startButton.text = context.getString(R.string.record)
    }

    fun addRecordComponents() {
        // Add button container to the FrameLayout
        addView(buttonContainer, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.BOTTOM
        })
        background = ResourcesCompat.getDrawable(resources, R.drawable.resizable_border, null)
        closeContainer.bringToFront()
    }

    fun switchToStopButton() {
        startButton.text = context.getString(R.string.stop)
    }

    fun switchToStartButton() {
        startButton.text = context.getString(R.string.record)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.rawX
                lastTouchY = event.rawY
                activePointerId = event.getPointerId(0)
            }
            MotionEvent.ACTION_MOVE -> {
                if (activePointerId != INVALID_POINTER_ID) {
                    val x = event.rawX
                    val y = event.rawY
                    val center = getCenterCoordinates()
                    val dx = x - lastTouchX
                    val dy = y - lastTouchY

                    if (abs(dx) > 5 || abs(dy) > 5) {
                        val params = layoutParams as WindowManager.LayoutParams

                        if (isInResizeRange(x - center.first, y - center.second)) {
                            // Resize the view
                            params.width = (width + dx).toInt().coerceAtLeast(minWidth)
                            params.height = (height + dy).toInt().coerceAtLeast(minHeight)
                        }
                        else {
                            // Move the view
                            params.x += dx.toInt()
                            params.y += dy.toInt()
                        }

                        windowManager.updateViewLayout(this, params)

                        lastTouchX = x
                        lastTouchY = y
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                activePointerId = INVALID_POINTER_ID
            }
        }
        return true
    }


    private fun isInResizeRange(x: Float, y: Float): Boolean {
        val threshold = 50 // Increase threshold for easier resizing
        return (x > width / 2 - threshold || y > height / 2 - threshold)
    }

    private fun getCenterCoordinates(): Pair<Float, Float> {
        val location = IntArray(2)
        getLocationOnScreen(location)

        val x = location[0] + width / 2
        val y = location[1] + height / 2

        // Now you have the x and y coordinates of the view
        println("My coordinates: x=$x, y=$y")
        return x.toFloat() to y.toFloat()
    }

    companion object {
        private const val INVALID_POINTER_ID = -1
    }
}
