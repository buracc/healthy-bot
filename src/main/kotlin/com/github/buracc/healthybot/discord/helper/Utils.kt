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
}