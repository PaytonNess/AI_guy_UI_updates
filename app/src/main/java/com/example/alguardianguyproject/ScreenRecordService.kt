package com.example.alguardianguyproject

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.io.File

class ScreenRecordService : Service() {
    private lateinit var mediaProjection: MediaProjection
    private lateinit var mediaProjectionManager: MediaProjectionManager
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)
                val data = if (Build.VERSION.SDK_INT >= TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_DATA, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EXTRA_DATA)
                }

                if (resultCode == Activity.RESULT_OK && data != null) {
                    // Initialize MediaProjection and start recording
                    mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

                    val notification = createNotification()
                    startForeground(NOTIFICATION_ID, notification)

                    mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)

                    // Register callback and start recording logic
                    mediaProjection.registerCallback(object : MediaProjection.Callback() {
                        override fun onStop() {
                            // Handle when the MediaProjection stops
                            stopRecording()
                        }
                    }, null)

                    // 2. Start Recording Logic:
                    startRecording(intent, mediaProjection)
                }
            }
            ACTION_STOP -> stopRecording()
        }
        return START_STICKY
    }

    private fun startRecording(intent: Intent, mediaProjection: MediaProjection) {
        // Start the service in the foreground
        val rect = if (Build.VERSION.SDK_INT >= TIRAMISU) {
            intent.getParcelableExtra(EXTRA_RECORD_RECT, Rect::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_RECORD_RECT)
        }

        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(applicationContext)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setVideoEncodingBitRate(512 * 1000)
            setVideoFrameRate(30)
            setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT) // TODO: Use rectangle width and height
            setOutputFile(getOutputFile())
            prepare()
        }

        virtualDisplay = mediaProjection.createVirtualDisplay(
            "ScreenRecording",
            DISPLAY_WIDTH, DISPLAY_HEIGHT, DISPLAY_DPI,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mediaRecorder?.surface, null, null
        ) // TODO: Use rectangle width and height

        mediaRecorder?.start()
        isRecording = true
    }

    private fun stopRecording() {
        if (isRecording) {
            mediaRecorder?.stop()
            mediaRecorder?.reset()
            virtualDisplay?.release()
            mediaProjection.stop()
            isRecording = false
        }
    }

    private fun getOutputFile(): String {
        val fileName = "screen_record_${System.currentTimeMillis()}.mp4"
        return File(getExternalFilesDir(null), fileName).absolutePath
    }

    private fun createNotification(): Notification {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Screen Recording",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this,0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
            }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Screen Recording")
            .setContentText("Recording in progress...")
            .setSmallIcon(R.drawable.baked_goods_1) // TODO: Replace with recording icon
            .setContentIntent(pendingIntent)
            .build()
    }

    companion object {
        const val ACTION_START = "START_RECORDING"
        const val ACTION_STOP = "STOP_RECORDING"
        const val EXTRA_RESULT_CODE = "RESULT_CODE"
        const val EXTRA_DATA = "DATA"
        const val DISPLAY_WIDTH = 720
        const val DISPLAY_HEIGHT = 1280
        const val DISPLAY_DPI = 1
        const val EXTRA_RECORD_RECT = "RECORD_RECT"
        const val CHANNEL_ID = "screen_recording_channel"
        const val NOTIFICATION_ID =1
    }
}
