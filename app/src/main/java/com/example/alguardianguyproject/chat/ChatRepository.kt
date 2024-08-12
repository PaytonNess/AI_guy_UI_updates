package com.example.alguardianguyproject.chat

class ChatRepository {
    fun formatPrompt(message: String, transcription: String, messages: List<Message>): String {
        var prompt = ""
        if (transcription.isNotEmpty()) {
            prompt += "This is a description of a video the user sent to you: $transcription. *End of video description* "
        }
        if (messages.isNotEmpty()) {
            prompt += "Here is the conversation between you and the user: "
            for (m in messages) {
                prompt += if (m.isUser) {
                    "User: ${m.text} "
                } else {
                    "You: ${m.text} "
                }
            }
            prompt += "*End of conversation* "
        }

        prompt += "The user just sent you this: $message. "

        prompt += "*End of user message* Your purpose is to be help the user deal with difficult situations, or root out misinformation. Please respond to the user in a kind and helpful manner. And do it as if your a cool guy."
        return prompt
    }
}
