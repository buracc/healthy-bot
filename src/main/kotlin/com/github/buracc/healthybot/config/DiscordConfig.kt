package com.github.buracc.healthybot.config

import com.github.buracc.healthybot.config.properties.DiscordProperties
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(DiscordProperties::class)
class DiscordConfig(
    @Value("\${discord.bot.token}")
    private val botToken: String
) {
    @Bean
    fun jda(): JDA {
        return JDABuilder
            .createDefault(botToken)
            .enableIntents(GatewayIntent.GUILD_MEMBERS)
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .build()
            .awaitReady()
    }
}