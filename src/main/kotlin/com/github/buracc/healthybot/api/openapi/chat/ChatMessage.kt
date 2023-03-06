package com.github.buracc.healthybot.api.openapi.chat

data class ChatMessage(
    val role: String = "user",
    val content: String
)