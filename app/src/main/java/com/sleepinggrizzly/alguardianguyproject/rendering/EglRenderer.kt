package com.sleepinggrizzly.alguardianguyproject.rendering

import android.content.Context
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.media.Image
import android.opengl.GLES11Ext
import android.opengl.EGL14.eglGetCurrentContext
import android.opengl.GLES20
import android.opengl.GLES20.glFinish
import android.util.Log
import android.view.Surface
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import com.sleepinggrizzly.alguardianguyproject.R
import com.sleepinggrizzly.alguardianguyproject.rendering.EglSurface

class EglRenderer(
    private val surface: Surface,
    private val context: Context,
    private val imageWidth: Int,
    private val imageHeight: Int
) {
    private lateinit var eglSurface: EglSurface
    private lateinit var eglCore: EglCore
    private lateinit var image: Image
    private lateinit var rect: Rect
    private lateinit var surfaceTexture: SurfaceTexture

    private var programId = 0
    private var textureId = IntArray(1)
    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var texCoordBuffer: FloatBuffer
    private var vertexBufferId = 0
    private var texCoordBufferId = 0
    private val bufferIds = IntArray(2)

    private var positionHandle = 0
    private var texCoordHandle = 0
    private var textureHandle = 0
    private var overlayRegionHandle = 0

    var isBufferAvailable = true

    private fun initializeEGL() {
        Log.d("ThreadInfo", "initialize EGL Running on thread: ${Thread.currentThread().name}")
        try {
            eglCore = EglCore()
            eglCore.initialize()
            eglSurface = EglSurface(eglCore, surface)
            //eglSurface.bind()
        }
        catch (e: Exception) {
            Log.e("EglRenderer", "Error initializing EGL", e)
        }
    }

    private fun setupShaders() {
        try {
            Log.d("ThreadInfo", "setup shaders Running on context: ${eglGetCurrentContext()}")
            val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, R.raw.fragment_shader)
            val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, R.raw.vertex_shader)

            programId = GLES20.glCreateProgram()
            GLES20.glAttachShader(programId, vertexShader)
            GLES20.glAttachShader(programId, fragmentShader)
            GLES20.glLinkProgram(programId)

            GLES20.glUseProgram(programId)

            // Get attribute and uniform locations (assuming you have them in your shaders)
            positionHandle = GLES20.glGetAttribLocation(programId, "vPosition")
            texCoordHandle = GLES20.glGetAttribLocation(programId, "vTexCoord")
            textureHandle = GLES20.glGetUniformLocation(programId, "uTexture")
            overlayRegionHandle = GLES20.glGetUniformLocation(programId, "uOverlayRegion")
            Log.d("ThreadInfo", "setup shaders Running on thread: ${Thread.currentThread().name}")

            // Enable vertex attribute arrays
            GLES20.glEnableVertexAttribArray(positionHandle)
            GLES20.glEnableVertexAttribArray(texCoordHandle)

            // Bind the external texture (using GL_TEXTURE_EXTERNAL_OES)
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId[0])

            GLES20.glUniform1i(textureHandle, 0) // Set the texture unit to 0
        }
        catch (e: Exception) {
            Log.e("EglRenderer", "Error setting up shaders", e)
        }
    }

    private fun loadShader(type: Int, shaderResId: Int): Int {
        Log.d("ThreadInfo", "load shader Running on thread: ${Thread.currentThread().name}")

        val shaderCode = context.resources.openRawResource(shaderResId).bufferedReader().use { it.readText() }
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader,shaderCode)
        GLES20.glCompileShader(shader)
        return shader
    }

    private fun setupVertexBuffer() {
        Log.d("ThreadInfo", "setup vertex buffer Running on thread: ${Thread.currentThread().name}")
        val vertexData = floatArrayOf(
            -1.0f, -1.0f, 0.0f, 1.0f,  // Bottom left
            1.0f, -1.0f, 0.0f, 1.0f,  // Bottom right
            -1.0f,  1.0f, 0.0f, 1.0f,  // Top left
            1.0f,  1.0f, 0.0f, 1.0f   // Top right
        )

        vertexBuffer = ByteBuffer.allocateDirect(vertexData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertexData)
        vertexBuffer.position(0)

        vertexBufferId = bufferIds[0]
        // Bind vertex buffer and set data
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferId)
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexBuffer.capacity() * 4, vertexBuffer, GLES20.GL_STATIC_DRAW)
    }

    private fun setupTexCoordBuffer() {
        Log.d("ThreadInfo", "setup tex coord buffer Running on thread: ${Thread.currentThread().name}")

        val texCoordData = floatArrayOf(
            0.0f, 1.0f, // Bottom left
            1.0f, 1.0f, // Bottom right
            0.0f, 0.0f, // Top left
            1.0f, 0.0f  // Top right
        )

        texCoordBuffer = ByteBuffer.allocateDirect(texCoordData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(texCoordData)
        texCoordBuffer.position(0)

        texCoordBufferId = bufferIds[1]
        // Bind tex coord buffer and set data
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, texCoordBufferId)
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, texCoordBuffer.capacity() * 4, texCoordBuffer, GLES20.GL_STATIC_DRAW)
    }

    private fun createSurfaceTexture() {
        // Bind the texture (create if it doesn't exist)
        if (textureId[0] == 0) {
            Log.d("bindTexture", "creating new texture")
            textureId = IntArray(1)
            GLES20.glGenTextures(1, textureId, 0)

            // Create SurfaceTexture and bind it to this texture ID
            surfaceTexture = SurfaceTexture(textureId[0])
            surfaceTexture.setDefaultBufferSize(imageWidth, imageHeight)

            surfaceTexture.setOnFrameAvailableListener {
                Log.d("bindTexture", "onFrameAvailable")
            }

            // Check if the SurfaceTexture is already attached
//            try {
//                surfaceTexture.detachFromGLContext() // Detach if attached
//                checkGlError("detachFromGLContext")
//            } catch (e: Exception) {
//                // Ignore errors if it's not already attached
//                Log.d("bindTexture", "SurfaceTexture is not attached")
//            }
//            // Attach the SurfaceTexture to OpenGL context
//            surfaceTexture.attachToGLContext(textureId[0])
//            checkGlError("bind texture attachToGLContext")
        }
    }

    private fun bindTexture(image: Image) {
        // Determine texture target based on image format (assuming YUV_420_888)
        val target = GLES11Ext.GL_TEXTURE_EXTERNAL_OES
        checkGlError("bind texture target")

        // Get the appropriate texture format (assuming YUV_420_888)
//        val format = GLES20.GL_NONE // Or a suitable format based on your processing
//        checkGlError("bind texture format")

        // Activate the texture unit
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        checkGlError("bind texture glActiveTexture")

        createSurfaceTexture()

        Log.d("bindTexture", "texture id: ${textureId[0]}")
        GLES20.glBindTexture(target, textureId[0])
        checkGlError("bind texture glBindTexture")
        Log.d("bindTexture", "image width: ${image.width}, image height: ${image.height}")
        Log.d("bindTexture", "image planes: ${image.planes.size}")
        Log.d("bindTexture", "image hardware buffer: ${image.hardwareBuffer}")

        // Load image data into the texture
//        GLES20.glTexImage2D(
//            target, 0, format,image.width, image.height, 0,
//            format, GLES20.GL_UNSIGNED_BYTE, image.planes[0].buffer
//        )
//        checkGlError("bind texture glTexImage2D")

        // Set texture parameters (adjust as needed)
        GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        checkGlError("bind texture set params")

        // Generate mipmaps (optional, if needed)
        // GLES20.glGenerateMipmap(target)
    }

    fun release() {

//        if (textureId[0] != 0) {
//            GLES20.glDeleteTextures(1, textureId, 0)
//            textureId = IntArray(1)
//        }
//
        vertexBuffer.clear()
        texCoordBuffer.clear()
        GLES20.glDeleteBuffers(2, bufferIds, 0)
        GLES20.glDeleteProgram(programId)
        GLES20.glDeleteTextures(0, textureId, 0)
        GLES20.glDeleteFramebuffers(2, bufferIds, 0)
        GLES20.glDeleteRenderbuffers(2, bufferIds, 0)
        GLES20.glDeleteTextures(1, textureId, 0)

        eglSurface.release()
        eglCore.release()
    }

    fun end() {
        glFinish()
        eglSurface.end()
    }

    fun onSurfaceCreated() {
        initializeEGL()
        Log.d("ThreadInfo", "init Running on thread: ${Thread.currentThread().name}")
        setupShaders()
        GLES20.glGenBuffers(2, bufferIds, 0)
        setupVertexBuffer()
        setupTexCoordBuffer()
        // Unbind the buffer (good practice)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        createSurfaceTexture()
    }

    fun onDrawFrame() {
        isBufferAvailable = false
        Log.d("onDrawFrame", "onDrawFrame called")
        try {
            // 1. Bind the output Surface (if not already bound)
            eglSurface.bind()

            // 2. Update overlay region uniform
            GLES20.glUniform4f(
                overlayRegionHandle, rect.left.toFloat(), rect.top.toFloat(),
                rect.right.toFloat(), rect.bottom.toFloat()
            )
            checkGlError("glUniform4f")

            // 3. Bind the Image as a texture
            bindTexture(image)
            checkGlError("bindTexture")

            // Use surfaceTexture.updateTexImage() to update the texture data when needed
            //surfaceTexture.updateTexImage()
            //checkGlError("bind texture updateTexImage")

            // 4. Set viewport and clear colorGLES20.glViewport(0, 0, image.width, image.height)
            GLES20.glClearColor(0f, 0f, 0f, 1f) // Or your desired background color
            checkGlError("glClearColor")
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
            checkGlError("glClear")

            // 5. Use the program
            GLES20.glUseProgram(programId)
            checkGlError("glUseProgram")

            // 6. Draw a quad
            // Enable vertex attributes
            GLES20.glEnableVertexAttribArray(positionHandle)
            checkGlError("glEnableVertexAttribArray")
            GLES20.glEnableVertexAttribArray(texCoordHandle)
            checkGlError("glEnableVertexAttribArray")

            // Bind vertex buffer and set vertex attribute
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferId)
            checkGlError("glBindBuffer")

            // Set vertex and texture coordinate data
            GLES20.glVertexAttribPointer(
                positionHandle, 2, GLES20.GL_FLOAT, false, 0, 0
            )
            checkGlError("glVertexAttribPointer")

            // Bind tex coord buffer and set texture coordinate attribute
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, texCoordBufferId)
            checkGlError("glBindBuffer")
            GLES20.glVertexAttribPointer(
                texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, 0
            )
            checkGlError("glVertexAttribPointer")

            surfaceTexture.updateTexImage()
            checkGlError("bind texture updateTexImage")

            // Draw the quad
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
            checkGlError("glDrawArrays")

            // Disable vertex attributes
            GLES20.glDisableVertexAttribArray(positionHandle)
            checkGlError("glDisableVertexAttribArray")
            GLES20.glDisableVertexAttribArray(texCoordHandle)
            checkGlError("glDisableVertexAttribArray")
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
            checkGlError("glBindBuffer")

            // 7. Swap buffers to display the rendered frame
            eglSurface.swapBuffers()
            checkGlError("swapBuffers")

            // 8. Unbind the output Surface
            eglSurface.unbind()

            // 9. Release the Image
            //image.hardwareBuffer?.close()
            image.close()
        }
        catch (e: Exception) {
            Log.e("EglRenderer", "Error rendering frame", e)
            image.hardwareBuffer?.close()
            image.close()
        }
    }

    fun setImage(newImage: Image) {
        image = newImage
    }

    fun setRect(newRect: Rect) {
        rect = newRect
    }

    private fun checkGlError(op: String) {
        val error = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR) {
            Log.e("MyRenderer", "$op: glError $error")
            throw RuntimeException("$op: glError $error")
        }
    }
}
