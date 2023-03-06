package com.github.buracc.healthybot.api.openapi.text

import com.fasterxml.jackson.annotation.JsonProperty

data class TextRequest(
    val model: String,
    val prompt: String,
    val user: String,
    @JsonProperty("max_tokens")
    val maxTokens: Int = 512,
)