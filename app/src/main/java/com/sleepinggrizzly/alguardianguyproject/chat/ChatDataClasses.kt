package com.sleepinggrizzly.alguardianguyproject.chat

data class SendMessageRequest (
    val prompt: String,
    val model: String? = null,
    val company: String
)
