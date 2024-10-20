package com.example.alguardianguyproject.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.alguardianguyproject.VideoDiscussionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatViewModel(application: Application): AndroidViewModel(application) {
    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages
    private val chatRepository = ChatRepository()
    private val videoRepository = VideoDiscussionRepository()
    private var videoTranscription = ""

    init {
        _messages.value = emptyList() // Initialize with an empty list
    }

    fun sendMessage(message: String, modelName: String? = null, company: String) {
        viewModelScope.launch {
            val prompt =
                _messages.value?.let { chatRepository.formatPrompt(message, videoTranscription, it.toList()) }
            println("prompt: $prompt")

            withContext(Dispatchers.Main) {
                _messages.value = _messages.value?.plus(Message(message, true))
            }
            if (prompt != null) {
                val response = videoRepository.sendMessage(prompt, modelName, company)
                if (response.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        _messages.value = _messages.value?.plus(Message(response, false))
                    }
                }
            }
        }
    }

    fun setVideoTranscription(transcription: String) {
        videoTranscription = transcription
    }

    fun reset() {
        _messages.value = emptyList()
        videoTranscription = ""
    }
}