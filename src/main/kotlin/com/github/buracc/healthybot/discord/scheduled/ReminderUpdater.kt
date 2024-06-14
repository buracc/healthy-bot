package com.github.buracc.healthybot.discord.scheduled

import com.github.buracc.healthybot.service.ReminderService
import net.dv8tion.jda.api.entities.Guild
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime

@Component
class ReminderUpdater(
    private val guild: Guild,
    private val reminderService: ReminderService
) {
    companion object {
        private const val category = "1251168329532702820"
    }

    init {
        checkAndAnnounce()
    }

    @Scheduled(cron = "0 */10 * * * *")
    fun checkAndAnnounce() {
        val remindersToday = reminderService.getAll()
            .filter { it.remindDate.toLocalDate() == ZonedDateTime.now().toLocalDate() }
            .sortedBy { it.remindDate }
            .take(3)

        // check channels in category and delete ones that are not in remindersToday
        val category = guild.getCategoryById(category) ?: return

        category.voiceChannels.forEach { it.delete().queue() }

        for (reminder in remindersToday) {
            val now = Instant.now()
            val duration = Duration.between(now, reminder.remindDate.toInstant())
            val sb = StringBuilder()
            val days = duration.toDaysPart()
            val hours = duration.toHoursPart()
            val minutes = duration.toMinutesPart()
            if (days > 0) sb.append("$days days ")
            if (hours > 0) sb.append("$hours hours ")
            if (minutes > 0) sb.append("$minutes minutes")

            val eventTime = reminder.remindDate.toLocalTime().toString()

            guild.createVoiceChannel("$eventTime - ${reminder.message}")
                .setParent(category)
                .queue()
        }
    }
}