package com.example.alguardianguyproject

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.view.TextureView

class PreviewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextureView(context, attrs, defStyleAttr), TextureView.SurfaceTextureListener {
    private var surfaceTextureAvailable: Boolean = false

    init {
        surfaceTextureListener = this
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        surfaceTextureAvailable = true
        // Notify your EglRenderer or other components that the preview surface is ready
        // ...
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        // Handle preview size changes if needed
        // ...
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        surfaceTextureAvailable = false
        // Notify your EglRenderer or other components that the preview surface is destroyed
        // ...
        return true // Return true to indicate you've handled the destruction
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        // Called when the surface texture is updated (new frame available)
        // You might not need to handle this explicitly
    }
}