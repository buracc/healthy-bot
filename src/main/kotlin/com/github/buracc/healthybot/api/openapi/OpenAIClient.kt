package com.github.buracc.healthybot.api.openapi

import com.github.buracc.healthybot.api.openapi.chat.ChatRequest
import com.github.buracc.healthybot.api.openapi.chat.ChatResponse
import com.github.buracc.healthybot.api.openapi.image.ImageRequest
import com.github.buracc.healthybot.api.openapi.image.ImageResponse
import com.github.buracc.healthybot.api.openapi.text.TextRequest
import com.github.buracc.healthybot.api.openapi.text.TextResponse
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class OpenAIClient(
    private val openAIRestTemplate: RestTemplate
) {
    fun createChat(text: String, discordId: String) =
        openAIRestTemplate.postForObject(
            "/chat/completions",
            ChatRequest.create(text, discordId),
            ChatResponse::class.java
        )

    fun createText(text: String, discordId: String, model: String) =
        openAIRestTemplate.postForObject(
            "/completions",
            TextRequest(
                model = model,
                prompt = text,
                user = discordId
            ),
            TextResponse::class.java
        )

    fun createImage(text: String, discordId: String) = openAIRestTemplate.postForObject(
        "/images/generations",
        ImageRequest(
            prompt = text,
            user = discordId
        ),
        ImageResponse::class.java
    )
}