package com.github.buracc.healthybot.discord.helper

import com.github.buracc.healthybot.discord.exception.BotException
import net.dv8tion.jda.api.entities.Member

object Utils {
    private val explicitWords = listOf(
        "nigg", "fagg", "fags", "retard", "gger", "tard"
    )

    fun filterMember(text: String): (Member) -> Boolean {
        return {
            it.nickname?.lowercase()?.contains(text.lowercase()) == true
                    || it.effectiveName.lowercase().contains(text.lowercase())
                    || it.id == text.lowercase()
        }
    }

    fun filterMessageExplicitWords(text: String) {
        for (explicitWord in explicitWords) {
            if (text.contains(explicitWord)) {
                throw BotException("Your message contains explicit words.")
            }
        }
    }

    fun truncateText(text: String, maxSize: Int = 2000) = text.chunked(maxSize)
}