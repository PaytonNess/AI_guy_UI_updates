package com.sleepinggrizzly.alguardianguyproject

import com.sleepinggrizzly.alguardianguyproject.chat.SendMessageRequest
import com.sleepinggrizzly.alguardianguyproject.video.MediaItem
import com.sleepinggrizzly.alguardianguyproject.video.MediaRequest
import com.sleepinggrizzly.alguardianguyproject.video.RetrofitClient
import okhttp3.MultipartBody

class VideoDiscussionRepository {
    suspend fun uploadVideo(multipartBody: MultipartBody.Part): MediaRequest? {
        println("multipartBody headers: ${multipartBody.headers}")
        println("multipartBody body: ${multipartBody.body.contentType()}")
        val response = RetrofitClient.videoUploadService.uploadVideo(multipartBody)

        if (response.isSuccessful) {
            // Handle successful upload
            println("Video uploaded successfully: " + (response.body()?.name ?: "body is null"))
            return MediaRequest(response.body()?.name ?: "", response.body()?.uri ?: "")
        } else {
            // Handle upload error
            println("Video upload failed: " + response.errorBody()?.string())
            return null
        }
    }

    suspend fun checkStatus(files: List<String>): String {
        println("check status files: $files")
        val response = RetrofitClient.videoStatusService.checkStatus(files)

        if (response.isSuccessful) {
            val status = response.body()?.message

            // Handle successful upload
            println("Video uploaded successfully: $status")

            if (status == "SUCCESS" || status =="PROCESSING" || status == "FAILED") {
                return status
            }
            return "FAILED"
        } else {
            // Handle upload error
            println("Video upload failed: " + response.errorBody()?.string())
            return "FAILED"
        }
    }

    suspend fun transcribeVideo(files: List<MediaItem>): String {
        val response = RetrofitClient.sendFilesService.sendFiles(files)

        if (response.isSuccessful) {
            val message = response.body()?.message

            return message.toString()
        } else {
            // Handle upload error
            println("Video transcription failed: " + response.errorBody()?.string())
            return ""
        }
    }

    suspend fun deleteFile(file: String): String {
        val response = RetrofitClient.deleteFileService.deleteFile(file)
        if (response.isSuccessful) {
            val message = response.body()?.message
            return message.toString()
        } else {
            // Handle upload error
            println("Delete file failed: " + response.errorBody()?.string())
            return ""
        }
    }

    suspend fun sendMessage(message: String, model: String? = null, company: String): String {

        var request = SendMessageRequest(message, model, company)
        if (model.isNullOrEmpty()) {
            request = SendMessageRequest(message, null, company)
        }

        val response = RetrofitClient.sendMessageService.sendMessage(request)
        if (response.isSuccessful) {
            val responseMessage = response.body()?.message
            return responseMessage.toString()
        } else {
            // Handle upload error
            println("Send message failed: " + response.errorBody()?.string())
            return ""
        }
    }
}
