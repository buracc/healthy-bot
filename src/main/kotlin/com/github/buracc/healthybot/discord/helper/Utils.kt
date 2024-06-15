package com.github.buracc.healthybot.discord.helper

import net.dv8tion.jda.api.entities.Member
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object Utils {
    val TZ = ZoneId.of("Europe/Amsterdam")
    val FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm z")

    fun filterMember(text: String): (Member) -> Boolean {
        return {
            it.nickname?.lowercase()?.contains(text.lowercase()) == true
                    || it.effectiveName.lowercase().contains(text.lowercase())
                    || it.id == text.lowercase()
        }
    }

    fun truncateText(text: String, maxSize: Int = 2000) = text.chunked(maxSize)

    fun now(): ZonedDateTime = ZonedDateTime.now(TZ)

    fun parseDateTime(time: String): ZonedDateTime {
        return ZonedDateTime.parse(time, FORMAT)
    }
}