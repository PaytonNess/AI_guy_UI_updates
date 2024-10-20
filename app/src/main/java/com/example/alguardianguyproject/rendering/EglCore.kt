package com.example.alguardianguyproject.rendering

import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.util.Log

import android.view.Surface

class EglCore {
    var display: EGLDisplay = EGL14.EGL_NO_DISPLAY
    var context: EGLContext = EGL14.EGL_NO_CONTEXT
    private var config: EGLConfig? = null

    fun initialize() {
        display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (display == EGL14.EGL_NO_DISPLAY) {
            throw RuntimeException("Unable to get EGL14 display")
        }
        val version = IntArray(2)
        if (!EGL14.eglInitialize(display, version, 0, version, 1)) {
            throw RuntimeException("Unable to initialize EGL14")
        }

        val configAttributes = intArrayOf(
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL14.EGL_NONE
        )

        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        if (!EGL14.eglChooseConfig(display, configAttributes, 0, configs, 0, configs.size, numConfigs, 0)) {
            throw RuntimeException("Unable to find suitable EGLConfig")
        }
        config = configs[0]

        val contextAttributes = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL14.EGL_NONE
        )
        Log.d("ThreadInfo", "egl core Running on thread: ${Thread.currentThread().name}")
        context = EGL14.eglCreateContext(display, config, EGL14.EGL_NO_CONTEXT, contextAttributes, 0)
        if (context == EGL14.EGL_NO_CONTEXT) {
            throw RuntimeException("Unable to create EGLContext")
        }
    }

    fun createWindowSurface(surface: Surface): EGLSurface {
        val surfaceAttributes = intArrayOf(
            EGL14.EGL_NONE
        )
        //val eglSurface = EGL14.eglCreateWindowSurface(display, config, surface, surfaceAttributes, 0)
        val surfaceAttribs = intArrayOf(EGL14.EGL_WIDTH, 1, EGL14.EGL_HEIGHT, 1, EGL14.EGL_NONE)
        val eglSurface = EGL14.eglCreatePbufferSurface(display, config, surfaceAttribs, 0)
        EGL14.eglMakeCurrent(display, eglSurface, eglSurface, context)

        if (eglSurface == EGL14.EGL_NO_SURFACE) {
            throw RuntimeException("Unable to create EGLSurface for Surface")
        }
        return eglSurface
    }

    fun release() {
        if (display != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
            EGL14.eglTerminate(display)
            //EGL14.eglDestroyContext(display, context)
        }
        display = EGL14.EGL_NO_DISPLAY
        context = EGL14.EGL_NO_CONTEXT
        config = null
    }
}
