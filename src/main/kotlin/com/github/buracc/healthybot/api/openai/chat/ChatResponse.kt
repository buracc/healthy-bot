package com.github.buracc.healthybot.api.openai.chat

import java.time.Instant

data class ChatResponse(
    val created: Instant,
    val choices: List<Choice>
) {
    data class Choice(
        val index: Int,
        val message: ChatMessage
    )
}
