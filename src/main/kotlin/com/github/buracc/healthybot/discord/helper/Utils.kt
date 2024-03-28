package com.github.buracc.healthybot.discord.helper

import net.dv8tion.jda.api.entities.Member

object Utils {
    fun filterMember(text: String): (Member) -> Boolean {
        return {
            it.nickname?.lowercase()?.contains(text.lowercase()) == true
                    || it.effectiveName.lowercase().contains(text.lowercase())
                    || it.id == text.lowercase()
        }
    }

    fun truncateText(text: String, maxSize: Int = 2000) = text.chunked(maxSize)
}