package com.example.alguardianguyproject

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.example.alguardianguyproject.chat.ChatViewModel
import com.example.alguardianguyproject.chat.MessageAdapter

class OverlayService : Service(), ViewModelStoreOwner {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: ResizableOverlayView

    private lateinit var chatViewModel: ChatViewModel
    private lateinit var messageAdapter: MessageAdapter
    private val appViewModelStore: ViewModelStore by lazy { ViewModelStore() }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 100

        overlayView = ResizableOverlayView(this)
        windowManager.addView(overlayView, params)

        // Initialize ViewModel
        chatViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(ChatViewModel::class.java)

        // Initialize MessageAdapter
        messageAdapter = MessageAdapter() // Start with an empty list

        overlayView.setChatAdapter(messageAdapter)

        // Observe messages from ViewModel
        chatViewModel.messages.observeForever { messages ->
            messageAdapter.updateMessages(messages)
        }

        // Set up send button click listener
        overlayView.sendButton.setOnClickListener {
            val newMessageText = overlayView.messageInput.text.toString()
            if (newMessageText.isNotBlank()) {
                chatViewModel.sendMessage(newMessageText)
                overlayView.messageInput.text.clear()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::overlayView.isInitialized) {
            windowManager.removeView(overlayView)
        }
        appViewModelStore.clear()
    }

    override val viewModelStore: ViewModelStore
        get() = appViewModelStore
}
