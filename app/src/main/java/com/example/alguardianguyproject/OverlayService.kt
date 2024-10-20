package com.example.alguardianguyproject

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.alguardianguyproject.chat.ChatViewModel
import com.example.alguardianguyproject.chat.MessageAdapter
import com.example.alguardianguyproject.video.RecordAdapter

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: ResizableOverlayView

    private lateinit var chatViewModel: ChatViewModel
    private lateinit var messageAdapter: MessageAdapter

    private lateinit var recordViewModel: RecordViewModel

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val params = WindowManager.LayoutParams(
            400,
            500,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 100

        overlayView = ResizableOverlayView(this, windowManager)
        windowManager.addView(overlayView, params)

        // Initialize ViewModel
        chatViewModel = ViewModelProvider(
            MyApplication.sharedViewModelStoreOwner,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[ChatViewModel::class.java]

        recordViewModel = ViewModelProvider(
            MyApplication.sharedViewModelStoreOwner,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[RecordViewModel::class.java]
        // Initialize MessageAdapter
        messageAdapter = MessageAdapter() // Start with an empty list

        overlayView.setChatAdapter(messageAdapter)

        // Observe messages from ViewModel
        chatViewModel.messages.observeForever { messages ->
            messageAdapter.updateMessages(messages)
        }

        // Observe ViewModel changes and update UI
        recordViewModel.completedStages.observeForever { uiState ->
            recordViewModel.progress.value?.let { updateUi(uiState, it) }
        }
        recordViewModel.progress.observeForever { progress ->
            recordViewModel.completedStages.value?.let { updateUi(it, progress) }
        }

        // Set up send button click listener
        overlayView.sendButton.setOnClickListener {
            val newMessageText = overlayView.messageInput.text.toString()
            if (newMessageText.isNotBlank()) {
                // TODO: comment for production
                chatViewModel.sendMessage(newMessageText, overlayView.modelTextField.text.toString(), overlayView.modelSpinner.selectedItem.toString())
                // TODO: uncomment for production
//                chatViewModel.sendMessage(newMessageText, null, "Google")
                overlayView.messageInput.text.clear()
            }
        }

        setStartButtonListener()

        overlayView.chatButton.setOnClickListener {
            overlayView.removeLoadingComponents()
            overlayView.addChatComponents()
            overlayView.addMenuComponents()
            chatViewModel.setVideoTranscription(recordViewModel.getVideoTranscription())

            overlayView.setMenuButtonToClosedState()
            setMenuButtonListener()
        }

        overlayView.closeButton.setOnClickListener {
            stopRecording()
            stopSelf()
        }

        setMenuButtonListener()

        overlayView.menuHomeView.reviewSomethingButton.setOnClickListener {

            overlayView.removeMenuHomeView()

            startRecording()
            overlayView.recording = true

            overlayView.setMenuButtonToClosedState()
            overlayView.menuButton.setText(R.string.stop)
            overlayView.menuButton.setOnClickListener {
                stopRecording()
                overlayView.addLoadingComponents()
                overlayView.removeRecordComponents()
                overlayView.removeMenuComponents()
                overlayView.recording = false
            }
        }
//        overlayView.menuHomeView.savedConversations.setOnClickListener {
//            TODO("Not yet implemented")
//        }
//        overlayView.menuHomeView.settingsButton.setOnClickListener {
//            TODO("Not yet implemented")
//        }
//        overlayView.menuHomeView.humanAssistance.setOnClickListener {
//            TODO("Not yet implemented")
//        }
        overlayView.menuChatView.homeButton.setOnClickListener {
            overlayView.removeChatComponents()
            overlayView.removeMenuChatView()
            recordViewModel.reset()
            chatViewModel.reset()
        }
//        overlayView.menuChatView.deleteConversation.setOnClickListener {
//            TODO("Not yet implemented")
//        }
//        overlayView.menuChatView.settingsButton.setOnClickListener {
//            TODO("Not yet implemented")
//        }
//        overlayView.menuChatView.humanAssistance.setOnClickListener {
//            TODO("Not yet implemented")
//        }
    }

    private fun setMenuButtonListener() {
        overlayView.menuButton.setOnClickListener {
            if (overlayView.chatOpen) {
                if (overlayView.menuChatView.opened) {
                    overlayView.removeMenuChatView()
                    overlayView.setMenuButtonToClosedState()
                } else {
                    overlayView.addMenuChatView()
                    overlayView.setMenuButtonToOpenState()
                }
            }
            else {
                if (overlayView.menuHomeView.opened) {
                    overlayView.removeMenuHomeView()
                    overlayView.setMenuButtonToClosedState()
                } else {
                    overlayView.addMenuHomeView()
                    overlayView.setMenuButtonToOpenState()
                }
            }
        }
    }

    private fun setStartButtonListener() {
        overlayView.switchToStartButton()
    }

    private fun updateUi(completedStages: List<Int>, progress: Double) {
        val adapter = RecordAdapter(completedStages, progress)
        overlayView.recyclerView.adapter = adapter
    }

    private fun startRecording() {
        val options = ActivityOptionsCompat.makeCustomAnimation(
            this, R.anim.fade_out, R.anim.fade_out
        )
        val intent = Intent(this, RecordActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK // Required to start an Activity from a Service
        val rect = Rect()
        overlayView.getGlobalVisibleRect(rect)
        println("overlayView rect: $rect")
        intent.putExtra("RECT", rect)
        startActivity(intent, options.toBundle())
    }

    private fun stopRecording() {
        val intent = Intent(this, ScreenRecordService::class.java).apply {
            action = ScreenRecordService.ACTION_STOP
        }
        startService(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::overlayView.isInitialized) {
            windowManager.removeView(overlayView)
        }
        MyApplication.sharedViewModelStoreOwner.clearViewModelStore()
    }
}
