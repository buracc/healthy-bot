package com.github.buracc.healthybot.api.openai.chat

data class ChatMessage(
    val role: String = "user",
    val content: String
)