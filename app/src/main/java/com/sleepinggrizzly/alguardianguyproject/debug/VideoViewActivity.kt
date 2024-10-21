package com.sleepinggrizzly.alguardianguyproject.debug

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.sleepinggrizzly.alguardianguyproject.R
import java.io.File

class VideoViewActivity: AppCompatActivity() {
    private val videoView: VideoView by lazy { findViewById(R.id.videoView) }
    private lateinit var videoPath: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_video)
        try {
            videoPath = intent.getStringExtra("INTERNAL_FILE_PATH") ?: ""
            val videoFile = File(videoPath)
            val videoUri = Uri.fromFile(videoFile)

            val headers = mapOf("Content-Type" to "video/mp4")
            videoView.setVideoURI(videoUri, headers)

            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoFile.absolutePath)

            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
            val bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
            val mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
            val likelyCodec = when {
                mimeType?.contains("avc") == true -> "H.264"
                mimeType?.contains("hevc") == true -> "H.265"
                mimeType?.contains("vp9") == true -> "VP9"
                // Add more mappings as needed
                else -> "Unknown"
            }
            Log.d("VideoProperties", "Likely Codec: $likelyCodec")

            Log.d("VideoProperties", "Width: $width, Height: $height")
            Log.d("VideoProperties", "Duration: $duration ms, Bitrate: $bitrate")

            retriever.release()

            videoView.setVideoURI(videoUri)
            videoView.start()
        }
        catch (e: Exception) {
            Log.e("VideoViewActivity", "Error setting video URI: ${e.message}")
        }
    }
}