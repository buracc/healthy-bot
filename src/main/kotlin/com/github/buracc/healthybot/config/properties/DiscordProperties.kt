package com.github.buracc.healthybot.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotBlank

@ConfigurationProperties(prefix = "discord.bot")
@ConstructorBinding
@Validated
data class DiscordProperties(
    @NotBlank
    val token: String,
    @NotBlank
    val guildId: String
)