package com.github.buracc.healthybot.discord.helper

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import org.springframework.stereotype.Component
import java.awt.Color

@Component
class EmbedHelper(
    private val jda: JDA
) {
    fun builder(title: String): EmbedBuilder {
        return builder()
            .setTitle(title)
    }

    fun builder(): EmbedBuilder {
        return EmbedBuilder()
            .setColor(Color.YELLOW)
            .setFooter("HealthyBot")
    }
}