package com.github.buracc.healthybot.config

import com.github.buracc.healthybot.config.properties.DiscordProperties
import com.github.buracc.healthybot.discord.exception.BotException
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableConfigurationProperties(DiscordProperties::class)
@EnableScheduling
class DiscordConfig(
    private val discordProperties: DiscordProperties
) {
    @Bean
    fun jda() = JDABuilder
        .createDefault(discordProperties.token)
        .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
        .setMemberCachePolicy(MemberCachePolicy.ALL)
        .build()
        .awaitReady()

    @Bean
    fun guild(jda: JDA) = jda.getGuildById(discordProperties.guildId)
        ?: throw BotException("Guild not found")
}