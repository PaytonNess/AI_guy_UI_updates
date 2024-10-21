package com.sleepinggrizzly.alguardianguyproject.video

import com.sleepinggrizzly.alguardianguyproject.ApiConstants.DELETE_FILE_ENDPOINT
import com.sleepinggrizzly.alguardianguyproject.ApiConstants.GET_VIDEO_STATUS_ENDPOINT
import com.sleepinggrizzly.alguardianguyproject.ApiConstants.SEND_FILES_ENDPOINT
import com.sleepinggrizzly.alguardianguyproject.ApiConstants.SEND_MESSAGE_ENDPOINT
import com.sleepinggrizzly.alguardianguyproject.ApiConstants.UPLOAD_VIDEO_ENDPOINT
import com.sleepinggrizzly.alguardianguyproject.chat.SendMessageRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class UploadResponse(val message: String, val uploadResponse: Any? = null)

interface VideoUploadService {
    @Multipart
    @POST(UPLOAD_VIDEO_ENDPOINT)
    suspend fun uploadVideo(@Part videoFile: MultipartBody.Part): Response<MediaUploadResponse>
}

interface VideoStatusService {
    @POST(GET_VIDEO_STATUS_ENDPOINT)
    suspend fun checkStatus(@Body paths: List<String>): Response<UploadResponse>
}

interface SendFilesService {
    @POST(SEND_FILES_ENDPOINT)
    suspend fun sendFiles(@Body files: List<MediaItem>): Response<UploadResponse>
}

interface DeleteFileService {
    @POST(DELETE_FILE_ENDPOINT)
    suspend fun deleteFile(@Body file: String): Response<UploadResponse>
}

interface SendMessageService {
    @POST(SEND_MESSAGE_ENDPOINT)
    suspend fun sendMessage(@Body sendMessageRequest: SendMessageRequest): Response<UploadResponse>
}
