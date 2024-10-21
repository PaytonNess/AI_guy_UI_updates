package com.sleepinggrizzly.alguardianguyproject.video

data class MediaUploadResponse(
    val name: String,
    val uri: String,
    val uploadResponse: Any? = null
)
