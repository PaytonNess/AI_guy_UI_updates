package com.example.alguardianguyproject

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
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
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alguardianguyproject.chat.MessageAdapter

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
    private lateinit var backContainer: LinearLayout

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
        chatRecyclerView = inflater.findViewById<RecyclerView>(R.id.message_list)
        chatRecyclerView.layoutManager = LinearLayoutManager(context)
        messageInput = inflater.findViewById<EditText>(R.id.message_input)
        sendButton = inflater.findViewById<Button>(R.id.send_button)
        backButton = inflater.findViewById<Button>(R.id.back_button)
    }

    private fun initCloseButton(context: Context) {
        closeContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        val inflater = LayoutInflater.from(context).inflate(R.layout.close_button, closeContainer, true)
        closeButton = inflater.findViewById<Button>(R.id.CloseButton)
    }

    private fun addCloseButton() {
        addView(closeContainer, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.TOP or Gravity.END
        })
    }

    fun removeCloseButton() {
        removeView(closeContainer)
    }

    fun addChatComponents() {
        addView(chatContainer, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply {
            gravity = Gravity.BOTTOM
        })
        background = ResourcesCompat.getDrawable(resources, R.drawable.resizable_border, null)
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
        recyclerView = inflater.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        chatButton = inflater.findViewById<Button>(R.id.chatButton)
    }

    fun addLoadingComponents() {
        addView(loadingContainer, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.BOTTOM
        })
        background = ResourcesCompat.getDrawable(resources, R.drawable.resizable_border_colored, null)
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
        startButton = inflater1.findViewById<Button>(R.id.SendButton)
        startButton.text = "Record"
    }

    fun addRecordComponents() {
        // Add button container to the FrameLayout
        addView(buttonContainer, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.BOTTOM
        })
        background = ResourcesCompat.getDrawable(resources, R.drawable.resizable_border, null)
    }

    fun switchToStopButton() {
        startButton.text = "Stop"
    }

    fun switchToStartButton() {
        startButton.text = "Record"
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
                    val pointerIndex = event.findPointerIndex(activePointerId)
                    val x = event.rawX
                    val y = event.rawY

                    val dx = x - lastTouchX
                    val dy = y - lastTouchY

                    if (Math.abs(dx) > 5 || Math.abs(dy) > 5) {
                        val params = layoutParams as WindowManager.LayoutParams

                        // Move the view
                        params.x += dx.toInt()
                        params.y += dy.toInt()

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
        val threshold = 100 // Increase threshold for easier resizing
        return (x > width - threshold && y > height - threshold)
    }

    companion object {
        private const val INVALID_POINTER_ID = -1
    }
}
