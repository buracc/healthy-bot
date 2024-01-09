package com.github.buracc.healthybot.api.openai.chat

import com.fasterxml.jackson.annotation.JsonProperty

data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    @JsonProperty("max_tokens")
    val maxTokens: Int = 500,
    val user: String
) {
    companion object {
        fun create(
            text: String,
            discordId: String,
            model: String
        ) = ChatRequest(
            messages = listOf(ChatMessage(content = text)),
            model = model,
            user = discordId
        )
    }
}
