package com.example.alguardianguyproject

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: FrameLayout

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.LEFT
        params.x = 0
        params.y = 100

        // Create the overlay view
        overlayView = FrameLayout(this)

        // Add ComposeView to overlayView
        val composeView = ComposeView(this).apply {
            setContent {
                RecordControlScreens()
            }
        }
        overlayView.addView(composeView)

        windowManager.addView(overlayView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::overlayView.isInitialized) {
            windowManager.removeView(overlayView)
        }
    }
}

@Composable
fun RecordControlScreens() {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier.padding(all = 16.dp)
        ) {
            Button(onClick = {
                // You need to cast the context to your activity or use a method to handle recording
                if (context is MainActivity) {
                    context.startRecording()
                }
            }) {
                Text("Start Recording")
            }
        }
        Row(
            modifier = Modifier.padding(all = 16.dp)
        ) {
            Button(onClick = {
                // You need to cast the context to your activity or use a method to handle recording
                if (context is MainActivity) {
                    context.stopRecording()
                }
            }) {
                Text("Stop Recording")
            }
        }
    }
}
