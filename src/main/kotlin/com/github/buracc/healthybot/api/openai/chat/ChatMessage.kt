package com.github.buracc.healthybot.api.openai.chat

data class ChatMessage(
    val role: String = "user",
    var name: String? = null,
    val content: String
)