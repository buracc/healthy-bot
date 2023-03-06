package com.github.buracc.healthybot.api.openapi.chat

import com.fasterxml.jackson.annotation.JsonProperty

data class ChatRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<ChatMessage>,
    @JsonProperty("max_tokens")
    val maxTokens: Int = 500,
    val user: String
) {
    companion object {
        fun create(text: String, discordId: String) = ChatRequest(
            messages = listOf(ChatMessage(content = text)),
            user = discordId
        )
    }
}
