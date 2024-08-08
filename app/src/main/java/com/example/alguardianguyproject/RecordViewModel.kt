package com.example.alguardianguyproject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.example.alguardianguyproject.video.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class RecordViewModel: ViewModel() {

    private val _uiState = MutableStateFlow<Int>(0)
    val uiState: StateFlow<Int> = _uiState.asStateFlow()
    private val _progress = MutableStateFlow<Double>(0.0)
    val progress: StateFlow<Double> = _progress.asStateFlow()
    private val repository = VideoDiscussionRepository()
    private val fileManagement = FileManagementRepository()
    private var videoName = ""
    private var videoUri = ""
    private var audioName = ""
    private var audioUri = ""
    private var videoPath = ""
    private var audioMp3Path = ""
    private var audioPcmPath = ""
    private var videoTranscription = ""

    fun uploadVideo(videoPath: String) {
        val videoFile = File(videoPath)
        this.videoPath = videoPath

        // Create the request body and MultipartBody.Part
        val requestBody = videoFile.asRequestBody("video/mp4".toMediaTypeOrNull())
        val multipartBody = MultipartBody.Part.createFormData("mediaFile", videoFile.name, requestBody)

        viewModelScope.launch() {
            try {
                val response = repository.uploadVideo(multipartBody)
                if (response != null) {
                    videoUri = response.uri
                    videoName = response.name
                    withContext(Dispatchers.Main) {
                        _uiState.value = 1
                    }
                }
            } catch(e: Exception) {
                println("error uploading video: $e")
            }

            checkFileStatus()
        }
    }

    fun uploadAudio(audioPath: String, outputPath: String) {
        this.audioMp3Path = audioPath
        this.audioPcmPath = outputPath
        convertPcmToMp3WithProgress(audioPath, outputPath)
    }

    private fun convertPcmToMp3WithProgress(inputPath: String, outputPath: String) {
        println("input readable: " + File(inputPath).canRead())
        println("output writable: " + File(outputPath).canWrite())
        println("input: ${File(inputPath)}")
        val command = "-f s16le -ar 32000 -ac 1 -acodec pcm_s16le -i $inputPath -acodec libmp3lame $outputPath"

        FFmpeg.executeAsync(command
        ) { _, returnCode ->
            if (returnCode == Config.RETURN_CODE_SUCCESS) {
                println("Conversion successful")
                val audioFile = File(outputPath)
                println("audioFile: $audioFile")
                try {
                    // Create the request body and MultipartBody.Part
                    val requestBody = audioFile.asRequestBody("audio/mp3".toMediaTypeOrNull())
                    val multipartBody =
                        MultipartBody.Part.createFormData("mediaFile", audioFile.name, requestBody)
                    viewModelScope.launch() {
                        withContext(Dispatchers.Main) {
                            _uiState.value = 2
                        }
                        val response = repository.uploadVideo(multipartBody)
                        if (response != null) {
                            audioUri = response.uri
                            audioName = response.name
                            withContext(Dispatchers.Main) {
                                _uiState.value = 3
                            }
                        }
                    }
                } catch(e: Exception) {
                    println("error uploading audio: $e")
                }
            } else {
                println("Conversion failed with return code: $returnCode")
            }
        }

        // To track progress, you can use Config.enableStatisticsCallback()
        Config.enableStatisticsCallback { newStatistics ->
            val progressEstimate = if (newStatistics.size >0) {
                (newStatistics.videoFrameNumber.toFloat() / newStatistics.size) * 100
            } else {
                0f // Handle cases where size is zero to avoid division by zero
            }
            viewModelScope.launch() {
                withContext(Dispatchers.Main) {
                    _progress.value = progressEstimate.toDouble()
                }
            }
            println("Progress: ${progressEstimate.toInt()}%")
        }
    }

    @OptIn(InternalCoroutinesApi::class)
    private suspend fun checkFileStatus() {
        var processing = true;
        var status = "PROCESSING"
        while (processing) {
            viewModelScope.launch() {
                println("video: $videoName")
                println("audio: $audioName")
                val paths = listOfNotNull(
                    videoName.takeIf { it.isNotEmpty() },
                    audioName.takeIf { it.isNotEmpty() }
                )
                status = repository.checkStatus(
                    paths
                )
                if (status == "SUCCESS") {
                    processing = false
                }
            }
            if (processing) {
                delay(500)
            }
        }
        if (status == "SUCCESS") {
            transcribeVideo()
        }
    }

    private fun transcribeVideo() {
        viewModelScope.launch() {
            val transcriptionResponse = repository.transcribeVideo(
                listOfNotNull(
                    videoUri.takeIf { it.isNotEmpty() }?.let { MediaItem("video/mp4", it) },
                    audioUri.takeIf { it.isNotEmpty() }?.let { MediaItem("audio/mp3", it) }
                )
            )
            videoTranscription = transcriptionResponse
            if (transcriptionResponse.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    _uiState.value = 4
                }
            }

            if (videoPath.isNotEmpty()) {
                val response = fileManagement.deleteFile(videoPath)
                println("delete local video response: $response")
                if (response) {
                    withContext(Dispatchers.Main) {
                        _uiState.value = 5
                    }
                }
            }
            if (videoName.isNotEmpty()) {
                val response = repository.deleteFile(videoName)
                println("delete video response: $response")
                if (response == "SUCCESS") {
                    withContext(Dispatchers.Main) {
                        _uiState.value = 10
                    }
                }
            }
            if (audioPcmPath.isNotEmpty()) {
                val response = fileManagement.deleteFile(audioPcmPath)
                println("delete pcm audio response: $response")
                if (response) {
                    withContext(Dispatchers.Main) {
                        _uiState.value = 7
                    }
                }
            }
            if (audioMp3Path.isNotEmpty()) {
                val response = fileManagement.deleteFile(audioMp3Path)
                println("delete mp3 audio response: $response")
                if (response) {
                    withContext(Dispatchers.Main) {
                        _uiState.value = 8
                    }
                }
            }
            if (audioName.isNotEmpty()) {
                val response = repository.deleteFile(audioName)
                println("delete audio response: $response")
                if (response == "SUCCESS") {
                    withContext(Dispatchers.Main) {
                        _uiState.value = 9
                    }
                }
            }
        }
    }
}
