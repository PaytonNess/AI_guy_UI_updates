package com.sleepinggrizzly.alguardianguyproject.rendering

import android.opengl.EGL14
import android.opengl.EGLSurface
import android.util.Log
import android.view.Surface
import com.sleepinggrizzly.alguardianguyproject.rendering.EglCore

class EglSurface(private val eglCore: EglCore, surface: Surface) {
    private var eglSurface: EGLSurface = EGL14.EGL_NO_SURFACE
    private var isBound = false

    init {
        eglSurface = eglCore.createWindowSurface(surface)
    }

    fun bind() {
        if (eglSurface == EGL14.EGL_NO_SURFACE) {
            throw RuntimeException("EglSurface is not initialized")
        }
        if (!isBound) {
            if (!EGL14.eglMakeCurrent(eglCore.display, eglSurface, eglSurface, eglCore.context)) {
                // Handle error: Log or throw exception
                Log.e("EglSurface", "Unable to bind EGL context")
            }
            isBound = true
        }
    }

    fun unbind() {
        if (isBound) {
            if (!EGL14.eglMakeCurrent(eglCore.display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)) {
                // Handle error
                Log.e("EglSurface", "Unable to unbind EGL context")
            }
            isBound = false
        }
    }

    fun swapBuffers() {
        if (!EGL14.eglSwapBuffers(eglCore.display, eglSurface)) {
            // Handle error
            Log.e("EglSurface", "Unable to swap buffers")
        }
    }

    fun release() {
        if (eglSurface != EGL14.EGL_NO_SURFACE) {
            eglSurface = EGL14.EGL_NO_SURFACE
            unbind()
        }
    }

    fun end() {
        if (eglSurface != EGL14.EGL_NO_SURFACE) {
            EGL14.eglDestroySurface(eglCore.display, eglSurface)
        }
    }
}