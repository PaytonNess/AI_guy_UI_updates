package com.example.alguardianguyproject.video

data object VideoUploadUiState {
    var videoUploaded: Boolean = false
    var audioConverted: Boolean = false
    var conversionPercentage: Double = 0.0
    var audioUploaded: Boolean = false
    var transcriptionComplete: Boolean = false
    var serverVideoDeleted: Boolean = false
    var serverAudioDeleted: Boolean = false
    var localVideoDeleted: Boolean = false
    var localAudioDeleted: Boolean = false
}