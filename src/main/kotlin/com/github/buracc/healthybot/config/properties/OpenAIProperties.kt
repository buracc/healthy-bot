package com.github.buracc.healthybot.config.properties

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "openai")
data class OpenAIProperties(
    @NotBlank
    val secret: String,
    @NotNull
    val timeout: Long
)