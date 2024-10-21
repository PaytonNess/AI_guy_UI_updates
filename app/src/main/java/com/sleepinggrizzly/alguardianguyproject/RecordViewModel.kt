package com.sleepinggrizzly.alguardianguyproject

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.sleepinggrizzly.alguardianguyproject.video.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class RecordViewModel(application: Application): AndroidViewModel(application) {

    private val _completedStages = MutableLiveData(List(10) { 0 })
    val completedStages: LiveData<List<Int>> = _completedStages
    private val _progress = MutableLiveData(0.0)
    val progress: LiveData<Double> = _progress
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

        viewModelScope.launch {
            try {
                val response = repository.uploadVideo(multipartBody)
                if (response != null) {
                    videoUri = response.uri
                    println("videoUri: $videoUri")
                    videoName = response.name
                    withContext(Dispatchers.Main) {
                        updateCompletedStage(0, 1)
                    }
                }
                else {
                    throw Exception("Response is null")
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
                    viewModelScope.launch {
                        val response = repository.uploadVideo(multipartBody)
                        if (response != null) {
                            audioUri = response.uri
                            audioName = response.name
                            withContext(Dispatchers.Main) {
                                updateCompletedStage(1, 1)
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
            viewModelScope.launch {
                withContext(Dispatchers.Main) {
                    _progress.value = progressEstimate.toDouble()
                }
            }
            println("Progress: ${progressEstimate.toInt()}%")
        }
    }

    private suspend fun checkFileStatus() {
        var processing = true
        var status = "PROCESSING"
        while (processing) {
            viewModelScope.launch {
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
        viewModelScope.launch {
            val transcriptionResponse = repository.transcribeVideo(
                listOfNotNull(
                    videoUri.takeIf { it.isNotEmpty() }?.let { MediaItem("video/mp4", it) },
                    audioUri.takeIf { it.isNotEmpty() }?.let { MediaItem("audio/mp3", it) }
                )
            )
            videoTranscription = transcriptionResponse
            if (transcriptionResponse.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    updateCompletedStage(2, 1)
                }
            }

            if (videoPath.isNotEmpty()) {
                val response = fileManagement.deleteFile(videoPath)
                println("delete local video response: $response")
                if (response) {
                    withContext(Dispatchers.Main) {
                        updateCompletedStage(5, 1)
                    }
                }
            }
            if (videoName.isNotEmpty()) {
                val response = repository.deleteFile(videoName)
                println("delete video response: $response")
                if (response == "SUCCESS") {
                    withContext(Dispatchers.Main) {
                        updateCompletedStage(3, 1)
                    }
                }
            }
            if (audioPcmPath.isNotEmpty()) {
                val response = fileManagement.deleteFile(audioPcmPath)
                println("delete pcm audio response: $response")
                if (!response) {
                    withContext(Dispatchers.Main) {
                        updateCompletedStage(6, -1)
                    }
                }
            }
            if (audioMp3Path.isNotEmpty()) {
                val response = fileManagement.deleteFile(audioMp3Path)
                println("delete mp3 audio response: $response")
                if (response) {
                    withContext(Dispatchers.Main) {
                        updateCompletedStage(6, 1)
                    }
                }
            }
            if (audioName.isNotEmpty()) {
                val response = repository.deleteFile(audioName)
                println("delete audio response: $response")
                if (response == "SUCCESS") {
                    withContext(Dispatchers.Main) {
                        updateCompletedStage(4, 1)
                    }
                }
            }
        }
    }

    private fun updateCompletedStage(index: Int, isCompleted: Int) {
        val updatedList = _completedStages.value?.toMutableList() ?: mutableListOf()
        if (index in updatedList.indices) {
            updatedList[index] = isCompleted
            _completedStages.value = updatedList
        }
    }

    fun getVideoTranscription(): String {
        return videoTranscription
    }
    
    fun reset() {
        _completedStages.value = List(10) { 0 }
        _progress.value = 0.0
        videoName = ""
        videoUri = ""
        audioName = ""
        audioUri = ""
        videoPath = ""
        audioMp3Path = ""
        audioPcmPath = ""
        videoTranscription = ""
    }
}
