package com.example.alguardianguyproject.video

data class VideoUploadResponse(
    val message: String,
    val uploadResponse: UploadResponseData
)

data class UploadResponseData(
    val file: FileData
)

data class FileData(
    val name: String,
    val displayName: String,
    val mimeType: String,
    // ... other fields as needed
)
