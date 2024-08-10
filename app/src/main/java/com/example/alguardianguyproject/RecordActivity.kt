package com.example.alguardianguyproject

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat

class RecordActivity: AppCompatActivity() {
    private var isRecording = false
    private val requestCodeScreenCapture = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setTheme(android.R.style.Theme_Translucent_NoTitleBar) // Make the Activity invisible
        // ... rest of your initialization ...
        startRecording()
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
        }
        println("start screen recording")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(recordIntent)
        } else {
            startService(recordIntent)
        }

        isRecording = true
        finish()
    }

    fun startRecording() {
        println("start recording")
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
        // Use a default Rect if needed
        return Rect(0, 0, 100, 100)
    }
}