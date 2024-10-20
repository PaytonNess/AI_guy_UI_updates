package com.example.alguardianguyproject.debug

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class CopyFileService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val directoryUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra("directoryUri", Uri::class.java) ?: return START_NOT_STICKY
        } else {
            intent?.getParcelableExtra("directoryUri") ?: return START_NOT_STICKY
        }
        val filePath = intent.getStringExtra("EXTERNAL_FILE_PATH") ?: return START_NOT_STICKY
        println("directoryUri: $directoryUri")
        println("filePath: $filePath")
        // Use a coroutine or background thread for file operations
        CoroutineScope(Dispatchers.Main).launch {
            copyFileToExternalStorage(directoryUri, filePath)
        }

        return START_NOT_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    private fun copyFileToExternalStorage(directoryUri: Uri, filePath: String) {
        val context = applicationContext
        val sourceFile = File(filePath)
        if (!sourceFile.exists()) {
            Log.e("CopyFileService", "Source file does not exist: $filePath")
            return
        }
        println("sourceFile: $sourceFile")
        val directory = DocumentFile.fromTreeUri(context, directoryUri) ?: return
        println("directory: $directory")
        val newFile = directory.createFile("*/*", sourceFile.name) ?: return
        println("newFile uri: ${newFile.uri}")
        contentResolver.openOutputStream(newFile.uri)?.use { outputStream ->
            sourceFile.inputStream().use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }
}