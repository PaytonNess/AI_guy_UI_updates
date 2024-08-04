package com.example.alguardianguyproject

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import com.example.alguardianguyproject.ui.theme.AlGuardianGuyProjectTheme

class MainActivity : AppCompatActivity() {
    private lateinit var resizableOverlay: ResizableOverlayView
    private var isRecording = false
    private val requestCodeScreenCapture = 1
    private val OVERLAY_PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkOverlayPermission()
    }

    private fun checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
        } else {
            showOverlay()
        }
    }

    override fun onUserLeaveHint() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            enterPictureInPictureMode(PictureInPictureParams.Builder().build())
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // Handle the results for each permission
            permissions.entries.forEach {
                val permissionName = it.key
                val isGranted = it.value
                Log.d("Permissions", "$permissionName granted: $isGranted")
            }

            // Check if all required permissions are granted
            val allGranted = permissions.all { it.value }
            if (allGranted) {
                // All permissions granted, proceed with starting the service
                val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    screenCaptureLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
                } else {
                    @Suppress("DEPRECATION") // Suppress deprecation warning for older API levels
                    startActivityForResult(
                        mediaProjectionManager.createScreenCaptureIntent(),
                        requestCodeScreenCapture // Define a request code for this activity
                    )
                }
            } else {
                // Handle the case where some permissions are denied
                Toast.makeText(this, "Screen recording requires audio permission", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this)) {
                showOverlay()
            } else {
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showOverlay() {
        val intent = Intent(this, OverlayService::class.java)
        startService(intent)
    }

    private fun startScreenCapture() {
        val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val screenCaptureIntent = mediaProjectionManager.createScreenCaptureIntent()

        screenCaptureLauncher.launch(screenCaptureIntent)
    }

    private val screenCaptureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Handle successful screen capture permission grant
            val intentData = result.data ?: return@registerForActivityResult
            startScreenRecording(intentData, result)
        } else {
            // Handle permission denial
            Toast.makeText(this, "Screen capture permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startScreenRecording(intentData: Intent, result: ActivityResult) {
        // Handle successful screen capture permission grant
        val recordIntent = Intent(this, ScreenRecordService::class.java).apply {
            action = ScreenRecordService.ACTION_START
            putExtra(ScreenRecordService.EXTRA_RESULT_CODE, result.resultCode)
            putExtra(ScreenRecordService.EXTRA_DATA, intentData)
            putExtra(ScreenRecordService.EXTRA_RECORD_RECT, getOverlayRect())
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(recordIntent)
        } else {
            startService(recordIntent)
        }

        isRecording = true
        updateRecordingUI()
    }

    private fun updateRecordingUI() {
        // Update UI elements, e.g., toggle button states, show/hide overlays, etc.
        resizableOverlay.visibility = if (isRecording) View.VISIBLE else View.GONE
    }

    fun startRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14 and above
            requestPermissionLauncher.launch(arrayOf(
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.FOREGROUND_SERVICE,
                android.Manifest.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION
            ))
        } else {
            // Older Android versions that don't support ForegroundService
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestPermissionLauncher.launch(arrayOf(
                    android.Manifest.permission.RECORD_AUDIO
                ))
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), requestCodeScreenCapture)
            }
        }
    }

    fun stopRecording() {
        val intent = Intent(this, ScreenRecordService::class.java).apply {
            action = ScreenRecordService.ACTION_STOP
        }
        startService(intent)
        isRecording = false
    }

    private fun getOverlayRect(): Rect {
        val location = IntArray(2)
        resizableOverlay.getLocationOnScreen(location)
        return Rect(
            location[0],
            location[1],
            location[0] + resizableOverlay.width,
            location[1] + resizableOverlay.height
        )
    }

    override fun onPause() {
        super.onPause()
        if (isRecording) {
            stopRecording()
        }
    }
}
