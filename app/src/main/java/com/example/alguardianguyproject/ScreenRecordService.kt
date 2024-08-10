package com.example.alguardianguyproject

import android.Manifest
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import java.io.File
import java.io.FileOutputStream

class ScreenRecordService : Service() {
    private lateinit var mediaProjection: MediaProjection
    private lateinit var mediaProjectionManager: MediaProjectionManager
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var videoPath = ""
    private var audioPath = ""
    private var audioRecord: AudioRecord? = null
    private var audioThread: Thread? = null
    private var isAudioRecording = false
    private lateinit var viewModel: RecordViewModel

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)
                viewModel = ViewModelProvider(
                    MyApplication.sharedViewModelStoreOwner,
                    ViewModelProvider.AndroidViewModelFactory.getInstance(application)
                )[RecordViewModel::class.java]
                val data = if (Build.VERSION.SDK_INT >= TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_DATA, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EXTRA_DATA)
                }
                println("resultCode: $resultCode")
                println("data: $data")
                if (resultCode == Activity.RESULT_OK && data != null) {
                    // Initialize MediaProjection and start recording
                    println("made it to start recording")
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
                    println("mediaProjection: $mediaProjection")
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
        println("start recording in screen record service")
        val data = if (Build.VERSION.SDK_INT >= TIRAMISU) {
            intent.getParcelableExtra(EXTRA_DATA, Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_DATA)
        }
        val rect = if (Build.VERSION.SDK_INT >= TIRAMISU) {
            data?.getParcelableExtra<Rect>(EXTRA_RECORD_RECT, Rect::class.java)
        } else {
            @Suppress("DEPRECATION")
            data?.getParcelableExtra(EXTRA_RECORD_RECT)
        }
        videoPath = getOutputFile()
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
            setVideoSize(rect?.width() ?: DISPLAY_WIDTH, rect?.height() ?: DISPLAY_HEIGHT) // TODO: Use rectangle width and height
            setOutputFile(videoPath)
            if (rect != null) {
                setLocation(rect.exactCenterX(), rect.exactCenterY())
            }
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

        // Audio Recording Setup (Android 10+):
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startAudioRecording(mediaProjection)
        }
    }

    private fun startAudioRecording(mediaProjection: MediaProjection) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            val config = AudioPlaybackCaptureConfiguration.Builder(mediaProjection)
                    .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                    .build()

            val audioBufferSize = AudioRecord.getMinBufferSize(
                32000, // Sample rate
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            audioRecord = AudioRecord.Builder()
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(32000)
                        .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                        .build()
                )
                .setAudioPlaybackCaptureConfig(config)
                .setBufferSizeInBytes(audioBufferSize * 2)
                .build()

            audioRecord?.startRecording()
            isAudioRecording = true
            println("audioRecord: $audioRecord")
            audioThread = Thread {
                audioPath = getAudioPcmOutputFile()
                val audioData = ByteArray(audioBufferSize)
                val outputStream = FileOutputStream(audioPath)

                while (isAudioRecording && serviceScope.isActive) {
                    val bytesRead = audioRecord?.read(audioData, 0, audioBufferSize) ?: 0
                    if (bytesRead > 0) {
                        println("bytesRead: $bytesRead")
                        outputStream.write(audioData, 0, bytesRead) // Write audio data to file
                    }
                }
                outputStream.close() // Close the output stream when done
            }
            audioThread?.start()
        }
    }

    private fun stopRecording() {
        if (isRecording) {
            mediaRecorder?.stop()
            mediaRecorder?.reset()
            virtualDisplay?.release()
            mediaProjection.stop()
            isRecording = false
            viewModel.uploadVideo(videoPath)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                isAudioRecording = false
                audioRecord?.stop()
                audioRecord?.release()
                audioThread?.join() // Wait for audio thread to finish
                audioRecord = null
                audioThread = null
                viewModel.uploadAudio(audioPath, getAudioMp3OutputFile())
            }
        }
    }

    private fun getOutputFile(): String {
        val fileName = "screen_record_${System.currentTimeMillis()}.mp4"
        return File(filesDir, fileName).absolutePath
    }
    private fun getAudioPcmOutputFile(): String {
        val fileName = "audio_record_${System.currentTimeMillis()}.pcm"
        return File(filesDir, fileName).absolutePath
    }
    private fun getAudioMp3OutputFile(): String {
        val fileName = "audio_record_${System.currentTimeMillis()}.mp3"
        return File(filesDir, fileName).absolutePath
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

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel() // Cancel all coroutines in the scope
    }

    companion object {
        const val ACTION_START = "START_RECORDING"
        const val ACTION_STOP = "STOP_RECORDING"
        const val EXTRA_RESULT_CODE = "RESULT_CODE"
        const val EXTRA_DATA = "DATA"
        const val DISPLAY_WIDTH = 720
        const val DISPLAY_HEIGHT = 1280
        const val DISPLAY_DPI = 1
        const val EXTRA_RECORD_RECT = "RECT"
        const val CHANNEL_ID = "screen_recording_channel"
        const val NOTIFICATION_ID =1
    }
}
