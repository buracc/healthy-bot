package com.github.buracc.healthybot.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import jakarta.validation.constraints.NotBlank

@ConfigurationProperties(prefix = "discord")
@Validated
data class DiscordProperties(
    @NotBlank
    val token: String,
    @NotBlank
    val guildId: String,
)