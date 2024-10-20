package com.example.alguardianguyproject.recording

import android.media.ImageReader
import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import com.example.alguardianguyproject.ScreenRecordService

class RecordMediaCodecCallback: MediaCodec.Callback() {
    var screenRecordService: ScreenRecordService? = null

    override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
        Log.d("MediaCodec", "Input buffer available: $index")
//        if (screenRecordService?.renderOpenGl == false) {
//            screenRecordService?.processMediaCodecInput(index)
//        }
    }
    override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
//        screenRecordService?.processMediaCodecOutput(index, info)
    }

    override fun onError(p0: MediaCodec, p1: MediaCodec.CodecException) {
        Log.e("MediaCodec", "MediaCodec error: ${p1.message}")
    }

    override fun onOutputFormatChanged(p0: MediaCodec, p1: MediaFormat) {
        Log.d("MediaCodec", "Output format changed: $p1")
    }
}