package com.github.buracc.healthybot.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "discord.bot")
@ConstructorBinding
data class DiscordProperties(
    val token: String,
    val guildId: String
)