package com.github.buracc.healthybot.api.openai.chat

data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>
) {
    companion object {
        fun create(
            text: String,
            model: String
        ) = ChatRequest(
            messages = listOf(ChatMessage(name ="User", content = text)),
            model = model
        )
    }
}
