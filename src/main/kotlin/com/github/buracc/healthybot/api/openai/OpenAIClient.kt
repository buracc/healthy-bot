package com.github.buracc.healthybot.api.openai

import com.github.buracc.healthybot.api.openai.chat.ChatRequest
import com.github.buracc.healthybot.api.openai.chat.ChatResponse
import com.github.buracc.healthybot.api.openai.image.ImageRequest
import com.github.buracc.healthybot.api.openai.image.ImageResponse
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class OpenAIClient(
    private val openAIRestTemplate: RestTemplate
) {
    fun createChat(
        initialPrompt: String,
        model: String,
        text: String,
        discordId: String,
    ) = openAIRestTemplate.postForObject(
            "/chat/completions",
            ChatRequest.create(
                text = "$initialPrompt$text",
                discordId = discordId,
                model = model
            ),
            ChatResponse::class.java
        )

    fun createImage(
        initialPrompt: String,
        text: String,
        discordId: String
    ) = openAIRestTemplate.postForObject(
        "/images/generations",
        ImageRequest(
            prompt = "$initialPrompt$text",
            user = discordId
        ),
        ImageResponse::class.java
    )
}