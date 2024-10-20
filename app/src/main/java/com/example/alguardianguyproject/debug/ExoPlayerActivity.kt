package com.example.alguardianguyproject.debug

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS
import com.arthenica.mobileffmpeg.ExecuteCallback
import com.arthenica.mobileffmpeg.FFmpeg
import com.example.alguardianguyproject.R
import java.io.File

class ExoPlayerActivity: AppCompatActivity() {
    private var exoPlayer: ExoPlayer? = null
    private lateinit var videoPath: String

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exo_player)
        exoPlayer = ExoPlayer.Builder(this).build()
        val playerView: PlayerView = findViewById(R.id.playerView)
        playerView.player = exoPlayer

        videoPath = intent.getStringExtra("INTERNAL_FILE_PATH") ?: ""

        val videoFile = File(videoPath)
        val videoUri = Uri.fromFile(videoFile)

        val mediaItem = MediaItem.fromUri(videoUri)
        val mediaSource = ProgressiveMediaSource.Factory(DefaultDataSource.Factory(this)).createMediaSource(mediaItem)

        exoPlayer?.setMediaSource(mediaSource)
        exoPlayer?.prepare()
        exoPlayer?.play()

//        val command = arrayOf(
//            "-i", videoPath,
//            // Add desired encoding options (e.g., codec, bitrate, resolution)
//            "-c:v", "h264_mediacodec", "-b:v", "2M",outputFilePath
//        )
//
//        // Execute the command asynchronously
//        FFmpeg.executeAsync(command
//        ) { _, returnCode ->
//            when (returnCode) {
//                RETURN_CODE_SUCCESS -> {
//                    // Re-encoding successful
//                    Log.i("FFmpeg", "Re-encoding completed successfully.")
//                    // Handle the re-encoded file at outputFilePath
//
//                }
//
//                RETURN_CODE_CANCEL -> {// Re-encoding cancelled
//                    Log.i("FFmpeg", "Re-encoding cancelled.")
//                    finish()
//                }
//
//                else -> {
//                    // Re-encoding failed
//                    Log.e("FFmpeg", "Re-encoding failed with rc=$returnCode")
//                    // Handle the error
//                    finish()
//                }
//            }
//        }
    }

    override fun onStop() {
        super.onStop()
        exoPlayer?.release()
        exoPlayer = null
    }

    private fun getOutputFile(): String {
        val fileName = "screen_record_${System.currentTimeMillis()}.mp4"
        return File(filesDir, fileName).absolutePath
    }
}