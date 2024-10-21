package com.sleepinggrizzly.alguardianguyproject.recording

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.media.Image
import android.util.Log

class ProcessImageRepository {
    fun processImage(image: Image, overlayRegion: Rect): Bitmap {
        Log.d("ThreadInfo", "process Image Running on thread: ${Thread.currentThread().name}")
        try {
            // Convert the Image to a Bitmap (you might need to handle YUV to RGB conversion)
            val bitmap = imageToBitmap(image)

            // Create a mutable copy of the Bitmap for modification
            val processedBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

            // Iterate over the pixels in the overlay region and set them to transparent
            for (x in overlayRegion.left until overlayRegion.right) {
                for (y in overlayRegion.top until overlayRegion.bottom) {
                    processedBitmap.setPixel(x, y, Color.TRANSPARENT)
                }
            }

            return processedBitmap
        }
        catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    // Helper function to convert Image to Bitmap
    private fun imageToBitmap(image: Image): Bitmap {
        Log.d("ThreadInfo", "image to bitmap Running on thread: ${Thread.currentThread().name}")
        val plane = image.planes[0]
        val buffer = plane.buffer
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val rowPadding = rowStride - pixelStride * image.width

//        // Calculate the expected buffer size
//        val expectedBufferSize = (image.width + rowPadding / pixelStride) * image.height * pixelStride
//
//        // Check if the buffer's capacity is sufficient
//        if (buffer.capacity() < expectedBufferSize) {
//            throw IllegalArgumentException("Buffer is too small for the expected pixel data")
//        }

        val bitmap = Bitmap.createBitmap(
            image.width + rowPadding / pixelStride,
            image.height,
            Bitmap.Config.ARGB_8888
        )
        bitmap.copyPixelsFromBuffer(buffer)
        return bitmap
    }
}