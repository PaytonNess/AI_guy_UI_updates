package com.example.alguardianguyproject.debug

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class ExternalStorageActivity : AppCompatActivity() {

    private lateinit var filePath: String

    private val openDocumentTreeLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { directoryUri ->
        if (directoryUri != null) {// Pass this URI to your Service to initiate the copy process
            println("directoryUri: $directoryUri")
            startCopyService(directoryUri)
        }
        else {
            println("directoryUri is null")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ... (Set up your layout if needed)
        filePath = intent.getStringExtra("INTERNAL_FILE_PATH") ?: ""
        println("filePath: $filePath")
        openDocumentTreeLauncher.launch(null)
    }

    private fun startCopyService(directoryUri: Uri) {
        val intent = Intent(this, CopyFileService::class.java).apply {
            putExtra("directoryUri", directoryUri)
            // Add the path to the file in app storage
            putExtra("filePath", filePath)
        }
        startService(intent)
    }
}